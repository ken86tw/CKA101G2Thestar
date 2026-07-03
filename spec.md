# 專案規格書 — 0205_Maven_SpringBootMVC_Thymeleaf_Ver5_SpringData-Jpa

## 1. 專案簡介

這是一個 Spring Boot MVC + Thymeleaf + Spring Data JPA 的教學/練習專案，
以傳統 `EMP`（員工）/ `DEPT`（部門）範例資料為主題，示範一套完整的
Web 後台 CRUD 流程（新增、查詢、修改、刪除），並包含檔案（照片）上傳與
以 Blob 形式讀回圖片等進階用法。專案同時保留多組「示範/測試用」的
Controller，用來對照不同的 Spring 設定寫法（多資料來源、方法級驗證等）。

## 2. 技術棧

| 項目 | 內容 |
|---|---|
| 語言 / JDK | Java 17 |
| 框架 | Spring Boot 4.0.6（`spring-boot-starter-parent`） |
| Web | Spring MVC（`spring-boot-starter-webmvc`）、內嵌 Tomcat |
| 樣板引擎 | Thymeleaf（+ `thymeleaf-layout-dialect`） |
| 資料存取 | Spring Data JPA + Hibernate 7.2.12.Final |
| 資料庫 | MySQL 8（`com.mysql:mysql-connector-j`），Oracle 驅動亦已引入備用 |
| 驗證 | Jakarta Bean Validation（Hibernate Validator） |
| 其他 | Lombok、Gson、Log4j2（取代預設 Logback）、DevTools 熱重載 |
| 封裝方式 | `war` |

## 3. 資料模型

### 3.1 DEPT3（部門）
對應 Entity：`com.dept.model.DeptVO`

| 欄位 | 型別 | 說明 |
|---|---|---|
| DEPTNO | INT, PK, 自動遞增 | 部門編號 |
| DNAME | VARCHAR(14) | 部門名稱 |
| LOC | VARCHAR(13) | 部門地點 |

一個部門對多個員工（`@OneToMany(mappedBy="deptVO")`，cascade=ALL，eager fetch，依 `empno` 排序）。

初始資料：財務部（台北）、研發部（新竹）、業務部（紐約）、生管部（上海）。

### 3.2 EMP3（員工）
對應 Entity：`com.emp.model.EmpVO`

| 欄位 | 型別 | 驗證規則 |
|---|---|---|
| EMPNO | INT, PK, 自動遞增（從 7001 起） | — |
| ENAME | VARCHAR(10) | 不可空白；限中/英文字母、數字、底線，長度 2–10 |
| JOB | VARCHAR(9) | 不可空白；長度 2–10 |
| HIREDATE | DATE | 目前未啟用驗證（相關 `@NotNull/@Past/@Future` 已註解） |
| SAL | DECIMAL(7,2) | 不可空白；範圍 10000.00–99999.99 |
| COMM | DECIMAL(7,2) | 不可空白；範圍 1.00–99999.99 |
| UPFILES | LONGBLOB | 員工照片（二進位），未上傳由 Controller 手動報錯 |
| DEPTNO | INT, FK → DEPT3 | 多對一，`@ManyToOne` |

多對一員工屬於一個部門（`@ManyToOne` + `@JoinColumn(DEPTNO)`）。

初始資料：14 筆傳統 Scott/EMP 範例員工（KING、BLAKE、CLARK……MILLER）。

建表 SQL：`src/main/java/資料庫表格建立EMP3/表格建立_MySQL_EMP3.sql`（MySQL，含建庫、建表、種子資料）。

## 4. 功能需求（Web 端點）

### 4.1 首頁
- `GET /` — 首頁，顯示歡迎訊息（來自 `application.properties` 的 `welcome.message`）與一份說明清單 → `index.html`
- `GET /hello?name=xxx` — 帶參數版首頁，顯示自訂訊息

### 4.2 員工管理（`/emp/**`）

| 方法 | 路徑 | 說明 |
|---|---|---|
| GET | `/emp/listAllEmp` | 顯示所有員工清單 |
| GET | `/emp/addEmp` | 顯示新增員工表單 |
| POST | `/emp/insert` | 新增員工（含照片上傳，Bean Validation），成功後導回列表 |
| GET | `/emp/select_page` | 顯示「依員工編號查詢」頁面 |
| POST | `/emp/getOne_For_Display` | 依員工編號查詢單筆（方法級參數驗證：需 4 位數字、7001–7777），成功嵌入 `listOneEmp` 片段，失敗顯示錯誤訊息 |
| POST | `/emp/getOne_For_Update` | 依員工編號取得單筆資料，轉入修改表單 |
| POST | `/emp/update` | 修改員工資料（含可選擇是否更換照片），驗證失敗回表單，成功後顯示單筆結果 |
| POST | `/emp/delete` | 刪除員工，成功後回列表並顯示訊息 |
| GET | `/emp/DBGifReader?empno=` | 以 `image/gif` 回傳資料庫中存放的員工照片二進位資料；查無照片時回傳內建預設圖片 |

共用資料提供（`@ModelAttribute`）：
- `deptListData`：所有部門清單，供下拉選單使用
- `deptMapData`：固定的部門代碼/名稱對照表（示範另一種寫法）
- `empListData`：所有員工清單，供列表頁與查詢頁使用

### 4.3 驗證與錯誤處理
- 新增/修改採 `@Valid` + `BindingResult`，並手動排除 `upFiles` 欄位的驗證錯誤（因該欄位另有商業邏輯處理，見 4.4）。
- `getOne_For_Display` 採方法參數層級驗證（`@NotEmpty` `@Digits` `@Min` `@Max`），透過 `@ExceptionHandler`（`HandlerMethodValidationException` / `ConstraintViolationException`）統一攔截並組成錯誤訊息顯示於查詢頁。

### 4.4 檔案上傳
- 新增員工時必須上傳照片，未選擇檔案時顯示「員工照片: 請上傳照片」錯誤。
- 修改員工時，若未選擇新照片則沿用資料庫既有照片，否則以新檔案覆蓋。
- 照片以 `byte[]`（LONGBLOB）直接存於 `EMP3.upFiles` 欄位，讀取時透過 `/emp/DBGifReader` 端點以串流方式輸出。

## 5. 非功能 / 環境設定

- 資料庫連線：`jdbc:mysql://localhost:3306/db01`，帳號 `root`（見 `application.properties`，需與 `表格建立_MySQL_EMP3.sql` 建立的 `db01` 資料庫搭配使用）。
- 開發模式：`spring.thymeleaf.cache=false`（樣板即時更新，不需重啟）。
- 專案內含多組被註解掉的設定範例（訊息來源、Thymeleaf 路徑、MVC view 前後綴、上傳容量限制等），作為教學參考，預設均未啟用。
- Spring Security 相關依賴已預留但目前**未啟用**（`pom.xml` 中相關 dependency 均被註解）。

## 6. 教學/示範用附屬程式（非主要功能，供對照學習）

- `com.test.Autowired.*`：多資料來源（DataSource）自動裝配的不同寫法示範。
- `com.test.Autowired2.*`：Spring Data JPA 自動裝配示範。
- `Test_Application_CommandLineRunner.java`：啟動時執行程式碼的範例。
- `photoWrite/PhotoWrite.java`：圖片寫入相關工具程式。
- `Test-DataTable.html`：DataTables 前端元件示範頁（`webapp` 下，非 Thymeleaf 樣板）。

## 7. 已知限制 / 待確認事項

- `HIREDATE` 目前無任何驗證與格式綁定（`@DateTimeFormat` 等已註解），前端輸入格式需另行確認。
- 部門（DEPT）目前只有查詢功能被員工模組使用（下拉選單），未提供部門自身的新增/修改/刪除頁面，儘管 `DeptService` 已具備對應方法。
- Spring Security 尚未啟用，目前無登入/權限控管機制。
