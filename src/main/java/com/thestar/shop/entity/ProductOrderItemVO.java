package com.thestar.shop.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "PRODUCT_ORDER_ITEM")
public class ProductOrderItemVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRODUCT_ORDER_ITEM_ID")
    private Integer productOrderItemId;

    @Column(name = "SHOP_ORDER_ID", nullable = false)
    private Integer shopOrderId;

    @Column(name = "PRODUCT_ID", nullable = false)
    private Integer productId;

    @Column(name = "PROD_ORDER_ITEM_QTY", nullable = false)
    private Integer prodOrderItemQty;

    @Column(name = "PROD_ORDER_ITEM_PRICE", nullable = false)
    private Integer prodOrderItemPrice;

    @Column(name = "PROD_ORDER_REVIEW_STAT")
    private Byte prodOrderReviewStat;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID", insertable = false, updatable = false)
    private ProductsVO product;

    @ManyToOne
    @JoinColumn(name = "SHOP_ORDER_ID", insertable = false, updatable = false)
    private ShopOrderVO shopOrder;

    public Integer getProductOrderItemId() { return productOrderItemId; }
    public void setProductOrderItemId(Integer productOrderItemId) { this.productOrderItemId = productOrderItemId; }

    public Integer getShopOrderId() { return shopOrderId; }
    public void setShopOrderId(Integer shopOrderId) { this.shopOrderId = shopOrderId; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getProdOrderItemQty() { return prodOrderItemQty; }
    public void setProdOrderItemQty(Integer prodOrderItemQty) { this.prodOrderItemQty = prodOrderItemQty; }

    public Integer getProdOrderItemPrice() { return prodOrderItemPrice; }
    public void setProdOrderItemPrice(Integer prodOrderItemPrice) { this.prodOrderItemPrice = prodOrderItemPrice; }

    public Byte getProdOrderReviewStat() { return prodOrderReviewStat; }
    public void setProdOrderReviewStat(Byte prodOrderReviewStat) { this.prodOrderReviewStat = prodOrderReviewStat; }

    public ProductsVO getProduct() { return product; }
    public void setProduct(ProductsVO product) { this.product = product; }

    public ShopOrderVO getShopOrder() { return shopOrder; }
    public void setShopOrder(ShopOrderVO shopOrder) { this.shopOrder = shopOrder; }
}