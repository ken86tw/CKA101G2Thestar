package com.thestar.member.dto;

public class CouponAdminForm {

    private String couponCode;
    private String couponName;
    private String description;
    private Byte discountType;
    private Integer discountAmount;
    private Integer discountPercent;
    private Integer remainingQuantity;
    private Integer defaultValidDays;
    private Byte issueStatus = 0;

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public String getCouponName() {
        return couponName;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Byte getDiscountType() {
        return discountType;
    }

    public void setDiscountType(Byte discountType) {
        this.discountType = discountType;
    }

    public Integer getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Integer discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Integer getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Integer discountPercent) {
        this.discountPercent = discountPercent;
    }

    public Integer getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(Integer remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public Integer getDefaultValidDays() {
        return defaultValidDays;
    }

    public void setDefaultValidDays(Integer defaultValidDays) {
        this.defaultValidDays = defaultValidDays;
    }

    public Byte getIssueStatus() {
        return issueStatus;
    }

    public void setIssueStatus(Byte issueStatus) {
        this.issueStatus = issueStatus;
    }
}
