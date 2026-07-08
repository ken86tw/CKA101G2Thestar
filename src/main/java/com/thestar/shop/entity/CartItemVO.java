package com.thestar.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "CART_ITEM")
public class CartItemVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CART_ITEM_ID")
    private Integer cartItemId;

    @Column(name = "MEMBER_ID", nullable = false)
    private Integer memberId;

    @Column(name = "PRODUCT_ID", nullable = false)
    private Integer productId;

    @Column(name = "CART_ITEM_PROD_QTY", nullable = false)
    private Integer cartItemProdQty;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID", insertable = false, updatable = false)
    private ProductsVO product;

    public Integer getCartItemId() { return cartItemId; }
    public void setCartItemId(Integer cartItemId) { this.cartItemId = cartItemId; }

    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getCartItemProdQty() { return cartItemProdQty; }
    public void setCartItemProdQty(Integer cartItemProdQty) { this.cartItemProdQty = cartItemProdQty; }

    public ProductsVO getProduct() { return product; }
    public void setProduct(ProductsVO product) { this.product = product; }
}