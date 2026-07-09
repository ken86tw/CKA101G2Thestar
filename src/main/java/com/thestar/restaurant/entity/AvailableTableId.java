package com.thestar.restaurant.entity;

import java.io.Serializable;
import java.time.LocalDate; // 👈 1. 換成新版的 LocalDate
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/*
 * =====================================================================
 *  複合主鍵 (Composite Primary Key) 做法說明
 * =====================================================================
 *
 *  當一張 Table 的 PK 由「兩個以上欄位」共同組成時，JPA 有兩種做法：
 *    1. @IdClass   — 另外建一個「主鍵類別」並在 Entity 上宣告 @IdClass
 *    2. @EmbeddedId — 另外建一個「@Embeddable 主鍵類別」，
 *                       Entity 中以「單一屬性」搭配 @EmbeddedId 使用  ← 本範例採用此方式
 *
 *  AVAILABLE_TABLE 的複合主鍵為：DATE + SESSION_ID
 *  因此需要：
 *    步驟一：建立此 AvailableTableId.java，加上 @Embeddable
 *    步驟二：在 AvailableTableVO.java 中以 @EmbeddedId 使用它
 *
 *  必要條件：
 *    ① 這個類別必須加 @Embeddable
 *    ② 必須實作 Serializable
 *    ③ 必須覆寫 equals() 和 hashCode()（JPA 用來比對主鍵相等性）
 *    ④ 建議提供無參數建構子
 * =====================================================================
 */
@Embeddable
public class AvailableTableId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "DATE")
    private LocalDate date; // 👈 2. 這裡改成 LocalDate

    @Column(name = "SESSION_ID")
    private Integer sessionId;

    // ── 無參數建構子（JPA 必要）──
    public AvailableTableId() {}

    // ── 有參數建構子（方便程式使用）──
    public AvailableTableId(LocalDate date, Integer sessionId) { // 👈 3. 換成 LocalDate
        this.date = date;
        this.sessionId = sessionId;
    }

    // ── Getter / Setter 調整 ──
    public LocalDate getDate() { return date; } // 👈 換成 LocalDate
    public void setDate(LocalDate date) { this.date = date; } // 👈 換成 LocalDate

    public Integer getSessionId() { return sessionId; }
    public void setSessionId(Integer sessionId) { this.sessionId = sessionId; }

    // ── equals() 和 hashCode() 必須覆寫 ──
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AvailableTableId)) return false;
        AvailableTableId that = (AvailableTableId) o;
        return Objects.equals(date, that.date) &&
               Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, sessionId);
    }
}