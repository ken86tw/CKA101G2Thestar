# 員工登入 / 員工管理 / 員工權限管理 — 設計書

> 參考依據：`thestar_6.sql`（現行資料庫結構）、`security.md`（Spring Security 設計 guideline：
> 多重 SecurityFilterChain、BCrypt、集中式路徑權限、角色多對多）、`spec.md`（文件撰寫格式）。
> 本文件延續兩份 guideline 的精神，但落地在 `thestar1` 目前**純 REST/JSON、無 Thymeleaf**的架構上。

## 1. 現況分析

### 1.1 已具備的部分
- `EMPLOYEE`（`thestar_6.sql:356`）已有 `EMPLOYEE_PASSWORD VARCHAR(255)`，種子資料已用
  `$2a$12$hashedpassword00x` 格式預留 BCrypt 雜湊值，`LAST_LOGIN_TIME` 欄位也已預留。
- `DEPARTMENT`、`JOB_TITLE` 為獨立查詢表，`STATUS`（1=啟用/0=停用）是全庫一致的慣例欄位命名。
- `EMPLOYEE.STATUS` 代表在職狀態（1=在職，0=離職），需與「帳號是否可登入」的判斷合併考慮。

### 1.2 尚未具備、需要補的部分
- **完全沒有角色/權限資料表**。`JOB_TITLE`（總經理／部門主管／資深員工／一般員工／實習生）是職級
  描述，不是權限模型，不能拿來做 `hasRole()` 判斷。
- **沒有真正的登入機制**：`FakeLoginController`
  （`src/main/java/com/example/thestar1/member/controller/FakeLoginController.java:20-24`）
  用 `GET /dev/employeelogin/{employeeId}` 直接把 `employeeId` 塞進 `HttpSession`，不驗證密碼、
  不驗證帳號是否存在或在職。
- **沒有集中式授權機制**：`AdminOrderController`、`AdminStayRecordController` 等每一個方法都手動重複：
  ```java
  Integer employeeId = (Integer) session.getAttribute("loginEmployee");
  if (employeeId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  ```
  只判斷「有沒有登入」，完全沒有角色/權限分級。
- `pom.xml` 尚未加入 `spring-boot-starter-security`。
- `spring.jpa.hibernate.ddl-auto=validate`：新增 Entity 必須先在 `thestar_6.sql` 建好對應的表，
  兩邊要同步設計，不能只改一邊。

## 2. 設計目標

1. 用 Spring Security 取代手寫的 session 判斷，但**維持現有 REST/JSON 風格**（不導入 Thymeleaf、
   不用表單式登入頁），呼應 security.md 的 BCrypt + SecurityFilterChain 精神，改寫成 JSON 登入版本。
2. 新增獨立的角色（ROLE）／權限（PERMISSION）資料表，比照 security.md 的
   `EMP4` × `ROLES` × `EMP4_ROLES` 多對多設計，但再加一層 `PERMISSION`，讓「員工權限管理」
   可以在後台動態勾選權限、組成角色，而不是把權限寫死在程式碼字串裡。
3. 路徑/方法授權採 security.md「集中管理、由具體到抽象」的原則，並補上方法級
   `@PreAuthorize` 做細粒度權限控制。
4. 既有 Controller 的登入判斷邏輯改為由 Security 過濾器統一處理，Controller 內不再手寫
   `session.getAttribute("loginEmployee")`。
5. 提供員工管理（CRUD）與員工權限管理（角色/權限 CRUD、指派角色）兩個後台功能模組。

## 3. 資料庫設計（新增於 `thestar_6.sql`）

沿用既有慣例：`INT AUTO_INCREMENT` 主鍵、`STATUS TINYINT DEFAULT 1`、`InnoDB` +
`UTF8MB4_UNICODE_CI`、约束命名 `PK_/FK_/UQ_` 前綴。

```sql
-- ROLE：角色（例：系統管理員、櫃檯人員、房務主管）
CREATE TABLE ROLE (
    ROLE_ID     INT          NOT NULL AUTO_INCREMENT,
    ROLE_NAME   VARCHAR(50)  NOT NULL,          -- 顯示用名稱，例：櫃檯人員
    ROLE_CODE   VARCHAR(50)  NOT NULL,          -- 對應 Spring Security，例：ROLE_FRONT_DESK
    STATUS      TINYINT      NOT NULL DEFAULT 1 COMMENT '1=啟用, 0=停用',
    CONSTRAINT PK_ROLE PRIMARY KEY (ROLE_ID),
    CONSTRAINT UQ_ROLE_CODE UNIQUE (ROLE_CODE)
) ENGINE = INNODB DEFAULT CHARSET = UTF8MB4 COLLATE = UTF8MB4_UNICODE_CI;

-- PERMISSION：權限點（對應 @PreAuthorize 的最小授權單位）
CREATE TABLE PERMISSION (
    PERMISSION_ID   INT          NOT NULL AUTO_INCREMENT,
    PERMISSION_NAME VARCHAR(50)  NOT NULL,      -- 顯示用名稱，例：訂單查詢
    PERMISSION_CODE VARCHAR(50)  NOT NULL,      -- 例：ORDER_VIEW
    STATUS          TINYINT      NOT NULL DEFAULT 1 COMMENT '1=啟用, 0=停用',
    CONSTRAINT PK_PERMISSION PRIMARY KEY (PERMISSION_ID),
    CONSTRAINT UQ_PERMISSION_CODE UNIQUE (PERMISSION_CODE)
) ENGINE = INNODB DEFAULT CHARSET = UTF8MB4 COLLATE = UTF8MB4_UNICODE_CI;

-- ROLE_PERMISSION：角色 <-> 權限（多對多）
CREATE TABLE ROLE_PERMISSION (
    ROLE_ID       INT NOT NULL,
    PERMISSION_ID INT NOT NULL,
    CONSTRAINT PK_ROLE_PERMISSION PRIMARY KEY (ROLE_ID, PERMISSION_ID),
    CONSTRAINT FK_RP_ROLE       FOREIGN KEY (ROLE_ID) REFERENCES ROLE(ROLE_ID),
    CONSTRAINT FK_RP_PERMISSION FOREIGN KEY (PERMISSION_ID) REFERENCES PERMISSION(PERMISSION_ID)
) ENGINE = INNODB DEFAULT CHARSET = UTF8MB4 COLLATE = UTF8MB4_UNICODE_CI;

-- EMPLOYEE_ROLE：員工 <-> 角色（多對多，一位員工可身兼多角色）
CREATE TABLE EMPLOYEE_ROLE (
    EMPLOYEE_ID INT NOT NULL,
    ROLE_ID     INT NOT NULL,
    CONSTRAINT PK_EMPLOYEE_ROLE PRIMARY KEY (EMPLOYEE_ID, ROLE_ID),
    CONSTRAINT FK_ER_EMPLOYEE FOREIGN KEY (EMPLOYEE_ID) REFERENCES EMPLOYEE(EMPLOYEE_ID),
    CONSTRAINT FK_ER_ROLE     FOREIGN KEY (ROLE_ID) REFERENCES ROLE(ROLE_ID)
) ENGINE = INNODB DEFAULT CHARSET = UTF8MB4 COLLATE = UTF8MB4_UNICODE_CI;
```

放置順序：需接在 `EMPLOYEE` 之後、`NEWS` 之前（`thestar_6.sql:375` 之後），並在檔案開頭的
`DROP TABLE IF EXISTS` 區塊裡，於 `DROP TABLE IF EXISTS EMPLOYEE;` **之前**加入
`EMPLOYEE_ROLE`、`ROLE_PERMISSION`、`PERMISSION`、`ROLE` 的 DROP（先刪子表、再刪父表，
避免 FK 阻擋）。

### 3.1 種子資料建議（對應現有 `DEPARTMENT` / `JOB_TITLE` 種子資料）

```sql
INSERT INTO ROLE (ROLE_NAME, ROLE_CODE, STATUS) VALUES
('系統管理員', 'ROLE_SUPER_ADMIN', 1),
('人資主管',   'ROLE_HR_MANAGER',  1),
('資訊人員',   'ROLE_IT',          1),
('櫃檯人員',   'ROLE_FRONT_DESK',  1),
('房務人員',   'ROLE_HOUSEKEEPING',1);

INSERT INTO PERMISSION (PERMISSION_NAME, PERMISSION_CODE, STATUS) VALUES
('員工管理',     'EMPLOYEE_MANAGE',   1),
('角色權限管理', 'ROLE_MANAGE',       1),
('訂單查詢',     'ORDER_VIEW',        1),
('訂單退款',     'ORDER_REFUND',      1),
('入住/退房作業','STAYRECORD_CHECKIN',1),
('住宿紀錄查詢', 'STAYRECORD_VIEW',   1);

-- ROLE_PERMISSION、EMPLOYEE_ROLE 依實際帳號指派，範例：
-- 王客房（EMPLOYEE_ID 對應房務職）掛 ROLE_HOUSEKEEPING
-- 林人資掛 ROLE_HR_MANAGER，可用 EMPLOYEE_MANAGE + ROLE_MANAGE
```

> 是否要真的把這些 DDL/種子資料寫回 `thestar_6.sql`（共用檔案，隊友也在用），
> 建議先確認、談妥命名與初始角色清單後再動手，避免和隊友目前的 schema 使用方式衝突。

## 4. 安全架構設計

### 4.1 與 security.md guideline 的對照

| security.md 的設計 | 本專案的落地方式 |
|---|---|
| Thymeleaf 表單登入頁 `/admin/login` | 無頁面，改為 `POST /thestar/admin/login` 接收 JSON `{email, password}` |
| `SecurityFilterChain` 依 `@Order` 分層 | 單一 `AdminSecurityConfig`，`securityMatcher("/thestar/admin/**")`；`/thestar/admin/login` 放行，其餘需驗證 |
| 登入失敗導向 `/admin/login?error=true` | 自訂 `AuthenticationEntryPoint`/失敗處理，回 `401` + JSON 錯誤訊息 |
| `hasRole()`/`hasAnyRole()` 路徑授權 | 沿用，並加上方法級 `@PreAuthorize("hasAuthority('XXX_CODE')")` 做權限碼細粒度控管 |
| Remember-Me（`persistent_logins`） | 暫不導入；現行前端流程未見「記住我」需求，且會多一張表，先列為第 9 節待辦 |
| `AdminUserDetailsService` 讀 `EmpVO` + `RoleVO` | 改讀 `EMPLOYEE` + `EMPLOYEE_ROLE` + `ROLE` + `ROLE_PERMISSION` + `PERMISSION`，`GrantedAuthority` 同時包含角色碼與權限碼 |
| BCrypt | 沿用，`EMPLOYEE_PASSWORD` 種子資料格式已對齊 |

### 4.2 Session 策略

現行程式已大量依賴 `HttpSession`（`FakeLoginController`、各 Admin Controller），因此**不改用
JWT**，改用 Spring Security 內建的 session 機制：
`SecurityContextHolder` + `HttpSessionSecurityContextRepository`（Spring Security 預設），
登入成功後 Security 會把 `Authentication` 存進 session，之後每個 request 自動還原，
不需要額外的 token。這樣既得到集中授權，又不破壞既有 session-based 慣例。

### 4.3 GrantedAuthority 設計

`EmployeeUserDetails`（自訂 `UserDetails` 實作）回傳的 authorities 同時包含：
- 角色碼：`ROLE_SUPER_ADMIN`、`ROLE_FRONT_DESK`...（給 `hasRole()`/路徑層級用）
- 權限碼：`ORDER_VIEW`、`ORDER_REFUND`...（給 `@PreAuthorize("hasAuthority(...)")` 用）

即 `EMPLOYEE → EMPLOYEE_ROLE → ROLE`（角色本身）與
`EMPLOYEE → EMPLOYEE_ROLE → ROLE_PERMISSION → PERMISSION`（角色展開的權限）兩者聯集。

## 5. 員工登入設計

| 方法 | 路徑 | 說明 |
|---|---|---|
| POST | `/thestar/admin/login` | Body `{ "email": "...", "password": "..." }`；成功回 `200` + 員工基本資料（不含密碼）+ 角色/權限清單；同時更新 `EMPLOYEE.LAST_LOGIN_TIME`。失敗回 `401` + `{"error": "帳號或密碼錯誤"}` |
| POST | `/thestar/admin/logout` | 銷毀 session，回 `200` |
| GET  | `/thestar/admin/me` | 回傳目前登入員工資訊（供前端頁面載入時判斷登入狀態、顯示可操作選單） |

登入條件：`EMPLOYEE_MAIL` 存在、`EMPLOYEE_PASSWORD` 以 BCrypt 比對正確、且
`EMPLOYEE.STATUS = 1`（在職）才可登入；離職員工即使密碼正確也應被
`AdminUserDetailsService` 判為 `disabled`（實作 `UserDetails.isEnabled()` 回傳
`status == 1`），交給 Spring Security 統一擋下。

`/dev/employeelogin/{employeeId}`（`FakeLoginController`）在正式登入機制上線後應移除，
或明確限制只在本機開發 profile 下才註冊（例如 `@Profile("dev")`），避免正式環境繞過登入。

## 6. 員工管理設計（CRUD）

比照現有 `AdminOrderController`/`AdminStayRecordController` 的
`@RestController` + `@RequestMapping("/thestar/admin/...")` 風格：

| 方法 | 路徑 | 權限碼 | 說明 |
|---|---|---|---|
| GET | `/thestar/admin/employee` | `EMPLOYEE_MANAGE` | 員工清單（可依部門/職稱/在職狀態篩選、分頁） |
| GET | `/thestar/admin/employee/{id}` | `EMPLOYEE_MANAGE` | 單筆員工資料 |
| POST | `/thestar/admin/employee` | `EMPLOYEE_MANAGE` | 新增員工，密碼欄位輸入明碼，Service 層以 `PasswordEncoder.encode()` 雜湊後存入 |
| PUT | `/thestar/admin/employee/{id}` | `EMPLOYEE_MANAGE` | 修改基本資料（不含密碼、不含角色，角色走第 7 節端點） |
| PATCH | `/thestar/admin/employee/{id}/status` | `EMPLOYEE_MANAGE` | 在職/離職切換（`STATUS`），比照 `DEPARTMENT`/`JOB_TITLE` 的 1/0 慣例，不做實體刪除 |
| PATCH | `/thestar/admin/employee/{id}/password` | 本人或 `EMPLOYEE_MANAGE` | 修改密碼，需驗證舊密碼或由具權限者強制重設 |

設計要點：
- 不提供 `DELETE`：員工資料涉及訂單/入住紀錄等大量 FK 關聯（`ROOM_ORDER`、`STAY_RECORD` 等
  都可能以 `EMPLOYEE_ID` 記錄操作人），比照全庫 `STATUS` 停用慣例，用軟刪除。
- 新增/修改 DTO（`EmployeeCreateDTO`/`EmployeeUpdateDTO`）不可直接暴露 `EmployeeVO`，避免
  密碼雜湊值外流；回傳給前端的 DTO 一律排除 `EMPLOYEE_PASSWORD`。
- Email 唯一性沿用 `UQ_EMPLOYEE_MAIL`，新增/修改時由 DB 唯一約束 + Service 層檢查雙重把關。

## 7. 員工權限管理設計

| 方法 | 路徑 | 權限碼 | 說明 |
|---|---|---|---|
| GET | `/thestar/admin/role` | `ROLE_MANAGE` | 角色清單 |
| POST | `/thestar/admin/role` | `ROLE_MANAGE` | 新增角色（`ROLE_NAME` + `ROLE_CODE`） |
| PUT | `/thestar/admin/role/{id}` | `ROLE_MANAGE` | 修改角色名稱/停用 |
| GET | `/thestar/admin/permission` | `ROLE_MANAGE` | 權限點清單（給前端勾選 UI 用） |
| PUT | `/thestar/admin/role/{id}/permissions` | `ROLE_MANAGE` | 覆寫該角色的權限集合（Body: `permissionIds[]`） |
| PUT | `/thestar/admin/employee/{id}/roles` | `ROLE_MANAGE` | 覆寫該員工的角色集合（Body: `roleIds[]`） |

設計要點：
- 權限碼（`PERMISSION_CODE`）與程式碼中的 `@PreAuthorize` 字串必須保持一致，建議在
  `common` 套件放一個 `PermissionCodes`（`public static final String` 常數）讓 Controller
  引用，避免字串打錯；新增權限點時，DB 種子資料與常數類要同步更新（這是權限碼設計的
  已知取捨，比雜湊式全動態權限更容易維運與追蹤）。
- 角色/權限本身的變更（`ROLE`、`PERMISSION`、`ROLE_PERMISSION`）應該只有
  `ROLE_SUPER_ADMIN` 能操作，用 `@PreAuthorize("hasRole('SUPER_ADMIN')")` 額外收斂，
  避免一般擁有 `ROLE_MANAGE` 權限的主管把自己權限拉高（權限提升風險）。

## 8. 套件與類別藍圖

沿用現有 `com.example.thestar1.<module>.{entity,repository,service,controller,dto}` 分層：

```
com.example.thestar1.employee
 ├─ entity/       EmployeeVO, DepartmentVO, JobTitleVO, RoleVO, PermissionVO
 ├─ repository/   EmployeeRepository, RoleRepository, PermissionRepository
 ├─ service/      EmployeeService, RoleService, PermissionService
 ├─ controller/   EmployeeController, RoleController, PermissionController
 └─ dto/          EmployeeCreateDTO, EmployeeUpdateDTO, EmployeeViewDTO,
                   RoleDTO, AssignRoleDTO, AssignPermissionDTO

com.example.thestar1.auth
 ├─ controller/   AdminAuthController (login/logout/me)
 ├─ security/     AdminSecurityConfig (SecurityFilterChain, PasswordEncoder Bean)
 │                EmployeeUserDetailsService (implements UserDetailsService)
 │                EmployeeUserDetails (implements UserDetails)
 │                RestAuthenticationEntryPoint / RestAccessDeniedHandler（回傳 JSON 401/403）
 └─ common/       PermissionCodes（權限碼常數）
```

`EmployeeVO` 與 `RoleVO` 為 `@ManyToMany`（`EMPLOYEE_ROLE` 中間表，`FetchType.EAGER`，
比照 security.md `EmpVO`×`RoleVO` 的寫法）；`RoleVO` 與 `PermissionVO` 同理透過
`ROLE_PERMISSION` 多對多。

## 9. 既有 Controller 遷移對照

| 現況 | 遷移後 |
|---|---|
| `FakeLoginController` 手動塞 session | 移除或限定 `@Profile("dev")`，改由 `AdminAuthController` 真正登入 |
| `AdminOrderController` 每個方法手寫 `session.getAttribute("loginEmployee")==null` 檢查 | 移除手動檢查，改由 `SecurityFilterChain` 統一擋在 filter 層；需要員工身分時改用 `@AuthenticationPrincipal EmployeeUserDetails` 取得 `employeeId` |
| `AdminStayRecordController` 同上 | 同上，並針對 checkin/checkout 加 `@PreAuthorize("hasAuthority('STAYRECORD_CHECKIN')")` |
| `GlobalExceptionHandler` 目前只處理 `IllegalArgumentException`/`IllegalStateException`/`NoSuchElementException` | 新增 `AuthenticationException`/`AccessDeniedException` 的 `@ExceptionHandler`（若未走 `RestAuthenticationEntryPoint`），統一回傳格式 `{"error": "..."}` 維持與現有錯誤格式一致 |

## 10. 其他需同步調整的設定

- `pom.xml`：新增 `spring-boot-starter-security`（不需要
  `thymeleaf-extras-springsecurity6`，因為專案沒有 Thymeleaf）。
- `application.properties`：無需新增資料庫設定（沿用 `thestar` DB）；如需自訂 session
  逾時，可加 `server.servlet.session.timeout=30m` 之類設定。
- 密碼雜湊：需要一支類似 security.md 的 `BCrypt_encrypt` 小工具，把種子資料裡的
  `$2a$12$hashedpassword00x` 佔位字串換成真正的 BCrypt 雜湊，否則目前的種子帳號無法登入。

## 11. 待確認事項 / 風險

1. `thestar_6.sql` 是團隊共用檔案，本文件第 3 節的 DDL 屬於**設計提案**，實際寫入前建議先跟隊友
   確認初始角色/權限清單與資料表命名，避免與其他模組已在使用的 schema 產生衝突。
2. 是否要保留 Remember-Me（跨瀏覽器關閉仍保持登入）？現況未見明確需求，先不做，
   有需要再仿 security.md 的 `persistent_logins` 設計補上。
3. 權限碼與 `@PreAuthorize` 字串屬於雙重維護點（DB + 程式碼常數），需要建立「新增權限點」
   的固定流程（先加常數 + 加 DB 種子，再套用到 Controller），避免兩邊不同步。
4. `EMPLOYEE` 軟刪除（`STATUS=0`）後，歷史訂單/入住紀錄的 `EMPLOYEE_ID` FK 顯示邏輯
   （前端要秀出「已離職員工」字樣）需要在對應查詢 DTO 一併處理，不在本文件範圍內，
   留給員工管理頁面串接時一併設計。
