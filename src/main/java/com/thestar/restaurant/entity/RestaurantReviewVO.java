package com.thestar.restaurant.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// import com.member.model.MemberVO;

@Entity
@Table(name = "RESTAURANT_REVIEW")
public class RestaurantReviewVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer reviewId;
    private com.thestar.member.entity.MemberVO memberVO;                        // 替換為 MemberVO
    private RestaurantReservationVO restaurantReservationVO;
    private String reviewContent;
    private Integer reviewStars;

    @Id
    @Column(name = "REVIEW_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getReviewId() {
        return reviewId;
    }
    public void setReviewId(Integer reviewId) {
        this.reviewId = reviewId;
    }

    /*
     * FK → MEMBERS(MEMBER_ID)
     * 請將 Object 換成 MemberVO，並改為 @ManyToOne / @JoinColumn 寫法
     */
    @ManyToOne
    @jakarta.persistence.JoinColumn(name = "MEMBER_ID")
    @jakarta.validation.constraints.NotNull(message = "會員編號：不能空白")
    public com.thestar.member.entity.MemberVO getMemberVO() {
        return memberVO;
    }

    public void setMemberVO(com.thestar.member.entity.MemberVO memberVO) {
        this.memberVO = memberVO;
    }

    // FK → RESTAURANT_RESERVATION(RESERVATION_ID)
    @ManyToOne
    @JoinColumn(name = "RESERVATION_ID")
    @NotNull(message = "訂位編號：請勿空白")
    public RestaurantReservationVO getRestaurantReservationVO() {
        return restaurantReservationVO;
    }
    public void setRestaurantReservationVO(RestaurantReservationVO restaurantReservationVO) {
        this.restaurantReservationVO = restaurantReservationVO;
    }

    @Column(name = "REVIEW_CONTENT")
    @Size(max = 1000, message = "評論內容：長度不能超過 {max} 字元")
    public String getReviewContent() {
        return reviewContent;
    }
    public void setReviewContent(String reviewContent) {
        this.reviewContent = reviewContent;
    }

    @Column(name = "REVIEW_STARS")
    @NotNull(message = "評論星等：請勿空白")
    @DecimalMin(value = "1", message = "評論星等：最低 {value} 顆星")
    @DecimalMax(value = "5", message = "評論星等：最高 {value} 顆星")
    public Integer getReviewStars() {
        return reviewStars;
    }
    public void setReviewStars(Integer reviewStars) {
        this.reviewStars = reviewStars;
    }
}
