package com.thestar.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "PRODUCT_CATEGORY")
public class ProductCategoryVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRODUCT_CATEGORY_ID")
    private Integer productCategoryId;

    @Column(name = "PRODUCT_CATEGORY_NAME", nullable = false)
    private String productCategoryName;

    public Integer getProductCategoryId() { return productCategoryId; }
    public void setProductCategoryId(Integer productCategoryId) { this.productCategoryId = productCategoryId; }

    public String getProductCategoryName() { return productCategoryName; }
    public void setProductCategoryName(String productCategoryName) { this.productCategoryName = productCategoryName; }
}