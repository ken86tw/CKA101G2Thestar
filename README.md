# THE STAR Hotel & Residences

## 後台管理登入

登入網址：<http://localhost:8080/thestar/admin/login>

登入後導向後台首頁：`http://localhost:8080/thestar/admin/home`

### 測試帳號（密碼皆為 `Star2026!`）

| 帳號 | 密碼 | 角色 | 部門 |
| --- | --- | --- | --- |
| hr.lin@hotel.com | Star2026! | 人資主管 | 人資部 |
| it.chen@hotel.com | Star2026! | 系統管理員 | 資訊部 |
| room.wang@hotel.com | Star2026! | 房務人員 | 客房部 |
| food.zhang@hotel.com | Star2026! | 櫃檯人員 | 餐飲部 |

> 以上帳號密碼僅為本機開發／測試用途，已重設為統一的測試密碼，方便登入各角色權限視角。

## 前台網站

首頁：<http://localhost:8080/>

前台採前後端不分離的單頁應用（Vue 3 + Thymeleaf 後台），登入頁提供：

- **貴賓登入** / **員工登入**：以 ID 快速登入（開發用途，呼叫 `/dev/login/{memberId}`、`/dev/employeelogin/{employeeId}`，不驗證密碼）
- **關於我們 / 最新消息 / 文章**：免登入即可瀏覽的公開頁面，登入會員後可於文章下方留言

## 本機環境需求

- MySQL：`jdbc:mysql://localhost:3306/thestar`（帳號 `root` / 密碼 `123456`，見 `application.properties`）
- 綠界 ECPay 金鑰：需另外設定環境變數 `ECPAY_HASH_KEY`、`ECPAY_HASH_IV`（本機測試可用任意佔位字串，僅影響金流相關功能）

## 啟動方式

```bash
./mvnw spring-boot:run
```
