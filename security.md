# Spring_login3_Spring_Security_SecurityFilterChain_BCrypt 規格書

## 1. 專案概述

本專案是 Spring Boot + Spring Security 的教學範例，示範如何使用 **多重 `SecurityFilterChain`**
（前台／後台分離）、**BCrypt 密碼加密**、**記住我 (Remember-Me)** 與 **角色權限集中管理**，
建立一個同時擁有「前台會員」與「後台員工」兩套獨立登入機制的 Web 應用。

這是系列教學的第三支範例，前兩支（`Spring_login1_OncePerRequestFilter`、
`Spring_login2_OncePerRequestFilter_VIP`）使用自訂 `OncePerRequestFilter` 與明碼密碼；
本專案改用官方 `SecurityFilterChain` 介面並導入 BCrypt 加密，詳見
`src/main/java/三組登入範例的差異性說明/三組登入範例的差異性說明.txt`。

## 2. 技術棧

| 項目 | 版本／說明 |
|---|---|
| Java | 17 |
| Spring Boot | 4.0.6（`spring-boot-starter-parent`） |
| 打包方式 | WAR（可部署至外部 Tomcat，亦可用內嵌 Tomcat 啟動） |
| Web | spring-boot-starter-webmvc |
| 安全 | spring-boot-starter-security |
| 樣板引擎 | Thymeleaf（`spring-boot-starter-thymeleaf`、`thymeleaf-layout-dialect`、`thymeleaf-extras-springsecurity6`） |
| 持久層 | spring-boot-starter-data-jpa |
| 資料庫 | MySQL（`mysql-connector-j`），資料庫名稱 `db04B` |
| 密碼加密 | BCrypt（`BCryptPasswordEncoder`） |
| 日誌 | log4j2 |

## 3. 系統架構

### 3.1 雙軌登入設計

系統以路徑前綴區分兩套完全獨立的使用者體系：

- **前台會員（Frontend）**：路徑前綴 `/frontend/**`，對應資料表 `USERS`，登入後預設角色概念較單純。
- **後台員工（Admin）**：路徑前綴 `/admin/**`，對應資料表 `EMP4`，具備多角色（`ROLES`）權限管理。

兩者使用各自獨立的 `UserDetailsService`、`UserDetails` 實作、`SecurityFilterChain`，
彼此的 Session Cookie、Remember-Me Cookie、登入頁面互不干擾。

### 3.2 SecurityFilterChain 分層（依 `@Order` 由小到大依序匹配）

| Order | 類別 | securityMatcher | 說明 |
|---|---|---|---|
| 0 | `SecurityConfig1_StaticResource` | `/css/**`, `/js/**`, `/images/**`, `/webjars/**`, `/favicon.ico` | 靜態資源完全繞過 Security 過濾器鏈，全部放行，兼顧效能 |
| 1 | `AdminSecurityConfig` (`adminFilterChain`) | `/admin/**` | 後台專用鏈，含角色分級授權、表單登入、登出、Remember-Me |
| 2 | `FrontendSecurityConfig` (`frontendFilterChain`) | `/frontend/**` | 前台專用鏈，含表單登入、登出、Remember-Me |
| 99 | `SecurityConfig2_Default` | 其餘所有路徑 | 兜底規則，避免未被上述任何鏈匹配到的路徑出現例外，一律放行 |

### 3.3 全域共用元件

- `PasswordEncoderConfig`：全站唯一的 `PasswordEncoder` Bean（`BCryptPasswordEncoder`），
  前台、後台皆共用同一組密碼編碼器，不重複宣告。

## 4. 後台（Admin）規格

### 4.1 路徑權限（集中管理、由具體到抽象）

| 路徑 | 權限要求 |
|---|---|
| `/admin/login/**` | 免登入即可存取 |
| `/admin/protected1/**` | `ROLE_SUPER_ADMIN`、`ROLE_ADMIN`、`ROLE_EDITOR` 任一角色 |
| `/admin/protected2/**` | `ROLE_SUPER_ADMIN`、`ROLE_ADMIN` 任一角色 |
| `/admin/protected3/**` | 僅 `ROLE_SUPER_ADMIN` |
| `/admin/**` 其餘路徑 | 僅需登入（任何角色） |

### 4.2 登入設定

- 登入頁：`/admin/login`（自訂頁面，取代預設藍白頁）
- 帳號欄位：`username`；密碼欄位：`password`
- 登入成功：導向 `/admin/index`（若登入前有原欲訪問頁面，優先導回該頁）
- 登入失敗：導向 `/admin/login?error=true`

### 4.3 登出設定

- 登出網址：`/admin/logout`
- 登出成功導向：`/admin/login?logout`
- 銷毀 Session、清除 `JSESSIONID` Cookie

### 4.4 Remember-Me

- Cookie 名稱：`remember-me-admin`
- 簽署私鑰：`adminSecretKeyUnique`
- 有效期：604800 秒（7 天）
- **Token 儲存方式：資料庫持久化**（`PersistentTokenRepository` / `JdbcTokenRepositoryImpl`，對應資料表 `persistent_logins`）
  - 優點：導入 series（序列號）機制，Token 被竊取並重複使用時可被偵測，並強制使該使用者所有記住我連線失效，防止「動態撞庫」攻擊，安全性優於純 Cookie 雜湊機制。

### 4.5 資料模型（後台）

- `EmpVO`（對應資料表 `EMP4`）：`emp_id`、`username`、`password`（BCrypt 密文）、`enabled`，
  透過中間表 `EMP4_ROLES` 與 `RoleVO`（對應資料表 `ROLES`）形成多對多關聯（`@ManyToMany`, `FetchType.EAGER`）。
- 角色字串需帶 `ROLE_` 前綴（例如 `ROLE_ADMIN`）以對應 `hasRole()` / `hasAnyRole()` 規則。
- `AdminUserDetailsService` 讀取 `EmpVO` 及其角色集合，轉為 `GrantedAuthority` 清單，
  包裝成自訂 `AdminUserDetails` 回傳給 Spring Security。

## 5. 前台（Frontend）規格

### 5.1 路徑權限

| 路徑 | 權限要求 |
|---|---|
| `/frontend/protected1/**`、`/frontend/protected2/**` | 需登入（`authenticated()`） |
| `/frontend/**` 其餘路徑（含首頁、登入頁） | 全部放行 |

### 5.2 登入設定

- 登入頁：`/frontend/login`
- 帳號欄位：`account`；密碼欄位：`password`
- 登入成功：導向 `/frontend/index`（優先導回登入前欲訪問頁面）
- 登入失敗：導向 `/frontend/login?error=true`

### 5.3 登出設定

- 登出網址：`/frontend/logout`
- 登出成功導向：`/frontend/index?logout`
- 銷毀 Session、清除 `JSESSIONID` Cookie

### 5.4 Remember-Me

- Cookie 名稱：`remember-me-front`
- 簽署私鑰：`frontendSecretKeyUnique`
- 有效期：604800 秒（7 天）
- **Token 儲存方式：預設純 Cookie 雜湊機制**（`TokenBasedRememberMeServices`，未設定 `tokenRepository`）
  - 優點：免查資料庫、速度快。
  - 缺點：使用者若於其他裝置修改密碼，因雜湊值不符，Cookie 會自動失效（此為已知取捨，程式中預留了改用資料庫儲存的註解區塊）。

### 5.5 資料模型（前台）

- `UserVO`（對應資料表 `USERS`）：`id`、`account`、`password`（BCrypt 密文）、`enabled`。
- 前台目前不具備細緻角色機制，`FrontendUserDetailsService` 回傳的 `FrontendUserDetails`
  權限清單目前為 `null`（可依需求擴充，作法可參考後台 `AdminUserDetailsService`）。

## 6. 資料庫設計

資料庫：MySQL `db04B`（連線設定於 `src/main/resources/application.properties`）。
建表腳本：`src/main/java/資料庫表格建立USER_EMP_ROLES/表格建立_MySQL_USER_EMP_ROLES.sql`。

| 資料表 | 用途 |
|---|---|
| `USERS` | 前台會員帳號（`id`, `account`, `password`, `enabled`） |
| `EMP4` | 後台員工帳號（`emp_id`, `username`, `password`, `enabled`） |
| `ROLES` | 角色定義（`role_id`, `role_name`），內建 `ROLE_EDITOR`、`ROLE_ADMIN`、`ROLE_SUPER_ADMIN` |
| `EMP4_ROLES` | 員工與角色多對多中間表（`emp_id`, `role_id`） |
| `persistent_logins` | 後台 Remember-Me 的資料庫 Token 儲存表（`username`, `series`, `token`, `last_used`） |

### 測試帳號（密碼皆與帳號同名，已用 BCrypt 加密存入）

| 類型 | 帳號 | 角色 |
|---|---|---|
| 前台會員 | user1 ~ user5 | 無角色機制 |
| 後台員工 | peter1 | `ROLE_SUPER_ADMIN` |
| 後台員工 | peter2 | `ROLE_ADMIN` |
| 後台員工 | peter3 | `ROLE_EDITOR` |
| 後台員工 | staff4, staff5 | 無角色（登入後無法訪問任何 `protected*` 頁面） |

密碼加密工具：`src/main/java/bCrypt/BCrypt_encrypt.java`（用於離線產生上述測試帳號的 BCrypt 密文）。

## 7. 頁面路由（`IndexController_inSpringBoot`）

| HTTP 方法 | 路徑 | 回傳樣板 |
|---|---|---|
| GET | `/` | `index.html` |
| GET | `/frontend/index` | `frontend/index.html` |
| GET | `/frontend/login` | `frontend/login.html` |
| GET | `/frontend/frees/frees` | `frontend/frees/frees.html` |
| GET | `/frontend/protected1/protected1` | `frontend/protected1/protected1.html` |
| GET | `/frontend/protected2/protected2` | `frontend/protected2/protected2.html` |
| GET | `/admin/index` | `admin/index.html` |
| GET | `/admin/login` | `admin/login.html` |
| GET | `/admin/others/others` | `admin/others/others.html` |
| GET | `/admin/protected1/protected1` | `admin/protected1/protected1.html` |
| GET | `/admin/protected2/protected2` | `admin/protected2/protected2.html` |
| GET | `/admin/protected3/protected3` | `admin/protected3/protected3.html` |

## 8. 設定檔重點（`application.properties`）

- `spring.datasource.url=jdbc:mysql://localhost:3306/db04B?serverTimezone=Asia/Taipei`
- `spring.datasource.username=root` / `spring.datasource.password=123456`
- `spring.thymeleaf.cache=false`（開發階段關閉快取，即時反映樣板變更）

> 以上帳密為教學範例預設值，正式環境請務必改為安全的機密管理方式（如環境變數、Vault）。

## 9. 待辦／可擴充項目

1. 前台目前未實作角色機制，`FrontendUserDetailsService` 權限清單為 `null`，
   如需前台分級權限，可仿照 `AdminUserDetailsService` 補上 `GrantedAuthority` 轉換。
2. 前台 Remember-Me 若要提升安全性（防 Cookie 竊取／動態撞庫），
   可解除 `FrontendSecurityConfig` 中已預留的 `persistentTokenRepository()` 註解區塊，改用資料庫儲存。
3. `persistent_logins` 資料表目前採手動建表；若期望啟動時自動建表，
   可在 `AdminSecurityConfig.persistentTokenRepository()` 中解除
   `tokenRepository.setCreateTableOnStartup(true)` 的註解（僅建議用於開發環境）。
