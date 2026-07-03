package com.example.thestar1.order.entity;
import com.example.thestar1.refund.entity.RefundListVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "ROOM_ORDER")
public class OrderVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ORDER_ID", updatable = false)
    private Integer orderId;

    @Column(name = "MEMBER_ID")
    private Integer memberId;

    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    @Column(name = "COUPON_ID")
    private Integer couponId;

    @Column(name = "ORDER_STATUS")
    private Byte orderStatus;

    @Column(name = "CHECK_IN_DATE")
    private LocalDate checkInDate;

    @Column(name = "CHECK_OUT_DATE")
    private LocalDate checkOutDate;

    @Column(name = "TOTAL_AMOUNT")
    private Integer totalAmount;

    @Column(name = "DISCOUNT_AMOUNT")
    private Integer discountAmount;

    @Column(name = "PAID_AMOUNT")
    private Integer paidAmount;

    @Column(name = "MERCHANT_TRADE_NO")
    private String merchantTradeNo;

    @Column(name = "ECPAY_TRADE_NO")
    private String ecpayTradeNo;

    @Column(name = "PAYMENT_METHOD")
    private Byte paymentMethod;

    @CreationTimestamp
    @Column(name = "CREATED_TIME")
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "UPDATED_TIME")
    private LocalDateTime updatedTime;

    @JsonIgnore
    @OneToMany(mappedBy = "ordervo", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<OrderListVO> orderList = new ArrayList<>();

    @OneToOne(mappedBy = "ordervo", fetch = FetchType.LAZY)
    private RefundListVO refundvo;


    public OrderVO() {
        super();
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getCouponId() {
        return couponId;
    }

    public void setCouponId(Integer couponId) {
        this.couponId = couponId;
    }

    public Byte getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Byte orderStatus) {
        this.orderStatus = orderStatus;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Integer discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Integer getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Integer paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getMerchantTradeNo() {
        return merchantTradeNo;
    }

    public void setMerchantTradeNo(String merchantTradeNo) {
        this.merchantTradeNo = merchantTradeNo;
    }

    public String getEcpayTradeNo() {
        return ecpayTradeNo;
    }

    public void setEcpayTradeNo(String ecpayTradeNo) {
        this.ecpayTradeNo = ecpayTradeNo;
    }

    public Byte getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(Byte paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public RefundListVO getRefundvo() {
        return refundvo;
    }

    public void setRefundvo(RefundListVO refundvo) {
        this.refundvo = refundvo;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public List<OrderListVO> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<OrderListVO> orderList) {
        this.orderList = orderList;
    }

    public void addOrderList(OrderListVO item){
        orderList.add(item);
        item.setOrdervo(this);
    }
}
