package com.thestar.shop.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCT_REVIEW")
public class ProductReviewVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRODUCT_REVIEW_ID")
    private Integer productReviewId;

    @Column(name = "PRODUCT_ID", nullable = false)
    private Integer productId;

    @Column(name = "MEMBER_ID", nullable = false)
    private Integer memberId;

    @Column(name = "PRODUCT_ORDER_ITEM_ID", nullable = false)
    private Integer productOrderItemId;

    @Column(name = "PRODUCT_REVIEW")
    private String productReview;

    @Column(name = "PRODUCT_RATE", nullable = false)
    private Byte productRate;

    @Column(name = "PRODUCT_REVIEW_DATE", nullable = false)
    private LocalDateTime productReviewDate;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID", insertable = false, updatable = false)
    private ProductsVO product;

    public Integer getProductReviewId() { return productReviewId; }
    public void setProductReviewId(Integer productReviewId) { this.productReviewId = productReviewId; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }

    public Integer getProductOrderItemId() { return productOrderItemId; }
    public void setProductOrderItemId(Integer productOrderItemId) { this.productOrderItemId = productOrderItemId; }

    public String getProductReview() { return productReview; }
    public void setProductReview(String productReview) { this.productReview = productReview; }

    public Byte getProductRate() { return productRate; }
    public void setProductRate(Byte productRate) { this.productRate = productRate; }

    public LocalDateTime getProductReviewDate() { return productReviewDate; }
    public void setProductReviewDate(LocalDateTime productReviewDate) { this.productReviewDate = productReviewDate; }

    public ProductsVO getProduct() { return product; }
    public void setProduct(ProductsVO product) { this.product = product; }
}