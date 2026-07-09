package com.thestar.restaurant.entity;

import java.io.Serializable;
import java.sql.Date;

import com.thestar.member.entity.MemberVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "RESTAURANT_RESERVATION")
public class RestaurantReservationVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer reservationId;
    private MemberVO memberVO;        
    private BusinessHoursVO businessHoursVO;
    
    // === 新增：桌型編號外鍵屬性 ===
    private RestaurantTableVO restaurantTableVO;
    
    private Date date;
    private ReservationStatus reservationStatus;
    private Boolean reviewStatus;
    private String reservationRequest;

    @Id
    @Column(name = "RESERVATION_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getReservationId() {
        return reservationId;
    }
    public void setReservationId(Integer reservationId) {
        this.reservationId = reservationId;
 
    }

    /*
     * FK → MEMBERS(MEMBER_ID)
     */
    @ManyToOne
    @JoinColumn(name = "MEMBER_ID") 
    @NotNull(message = "會員編號：不能空白")
    public MemberVO getMemberVO() {
        return memberVO;
    }
    public void setMemberVO(MemberVO memberVO) {
        this.memberVO = memberVO;
    }

    /*
     * FK → BUSINESS_HOURS(SESSION_ID)
     */
    @ManyToOne
    @JoinColumn(name = "SESSION_ID")
    @NotNull(message = "時段：請勿空白")
    public BusinessHoursVO getBusinessHoursVO() {
        return businessHoursVO;
    }
    public void setBusinessHoursVO(BusinessHoursVO businessHoursVO) {
        this.businessHoursVO = businessHoursVO;
    }

    /*
     * =========================================================================
     * 新增：FK → TABLE_TYPE(TABLE_TYPE_ID)
     * 這裡遵循你將註解（Annotation）統一寫在 Getter 方法上的風格
     * =========================================================================
     */
    @ManyToOne
    @JoinColumn(name = "TABLE_TYPE_ID") // 對應資料庫的桌型編號外鍵欄位
    @NotNull(message = "桌型：請勿空白")
    public RestaurantTableVO getRestaurantTableVO() {
        return restaurantTableVO;
    }
    public void setRestaurantTableVO(RestaurantTableVO restaurantTableVO) {
        this.restaurantTableVO = restaurantTableVO;
    }

    @Column(name = "DATE")
    @NotNull(message = "訂位日期：請勿空白")
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }

    @Column(name = "RESERVATION_STATUS")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "訂位狀態：請勿空白")
    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }
    public void setReservationStatus(ReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    @Column(name = "REVIEW_STATUS")
    @NotNull(message = "評論狀態：請勿空白")
    public Boolean getReviewStatus() {
        return reviewStatus;
    }
    public void setReviewStatus(Boolean reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    @Column(name = "RESERVATION_REQUEST")
    @Size(max = 255, message = "特殊要求：長度不能超過 {max} 字元")
    public String getReservationRequest() {
        return reservationRequest;
    }
    public void setReservationRequest(String reservationRequest) {
        this.reservationRequest = reservationRequest;
    }
}