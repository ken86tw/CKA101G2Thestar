package com.thestar.restaurant.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/*
 * =====================================================================
 *  複合主鍵 Entity：使用 @EmbeddedId
 * =====================================================================
 *
 *  步驟二：在 Entity 中使用 @EmbeddedId 代替 @Id
 *
 *  重點：
 *    @EmbeddedId       → 宣告此屬性為複合主鍵物件（類型為 @Embeddable 類別）
 *    @ManyToOne        → 宣告與 BusinessHoursVO 的多對一關聯（SESSION_ID 為 FK）
 *    @MapsId("sessionId") → 告訴 JPA：SESSION_ID 這個 FK 欄位，
 *                           同時也對應到 AvailableTableId 裡的 sessionId 欄位，
 *                           避免欄位重複對應造成衝突
 * =====================================================================
 */
@Entity
@Table(name = "AVAILABLE_TABLE")
public class AvailableTableVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private AvailableTableId id;          // 複合主鍵物件
    private BusinessHoursVO businessHoursVO;
    private Integer largeTableCount;
    private Integer smallTableCount;

    // ── 複合主鍵：用 @EmbeddedId，不再用 @Id ──
    @EmbeddedId
    public AvailableTableId getId() {
        return id;
    }
    public void setId(AvailableTableId id) {
        this.id = id;
    }

    // ── FK 關聯：SESSION_ID → BUSINESS_HOURS(SESSION_ID) ──
    // @MapsId("sessionId") 表示這個關聯的 JoinColumn(SESSION_ID)
    // 同時也是複合主鍵 AvailableTableId 裡面的 sessionId 欄位
    @ManyToOne
    @MapsId("sessionId")
    @JoinColumn(name = "SESSION_ID")
    public BusinessHoursVO getBusinessHoursVO() {
        return businessHoursVO;
    }
    public void setBusinessHoursVO(BusinessHoursVO businessHoursVO) {
        this.businessHoursVO = businessHoursVO;
    }

    @Column(name = "LARGE_TABLE_COUNT")
    @NotNull(message = "大桌剩餘數量：請勿空白")
    public Integer getLargeTableCount() {
        return largeTableCount;
    }
    public void setLargeTableCount(Integer largeTableCount) {
        this.largeTableCount = largeTableCount;
    }

    @Column(name = "SMALL_TABLE_COUNT")
    @NotNull(message = "小桌剩餘數量：請勿空白")
    public Integer getSmallTableCount() {
        return smallTableCount;
    }
    public void setSmallTableCount(Integer smallTableCount) {
        this.smallTableCount = smallTableCount;
    }
}
