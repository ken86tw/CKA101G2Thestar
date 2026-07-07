package com.thestar.shop.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SHOP_ORDER")
public class ShopOrderVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SHOP_ORDER_ID")
    private Integer shopOrderId;

    @Column(name = "MEMBER_ID", nullable = false)
    private Integer memberId;

    @Column(name = "SHOP_ORDER_TIME", nullable = false)
    private LocalDateTime shopOrderTime;

    @Column(name = "SHOP_ORDER_TOTAL", nullable = false)
    private Integer shopOrderTotal;

    @Column(name = "SHOP_ORDER_PICKUP")
    private Byte shopOrderPickup;

    @Column(name = "SHOP_PAYMENT_STATUS")
    private Byte shopPaymentStatus;

    @Column(name = "SHOP_ORDER_STATUS")
    private Byte shopOrderStatus;

    @Column(name = "SHOP_ORDER_NAME", nullable = false)
    private String shopOrderName;

    @Column(name = "SHOP_ORDER_PHONE", nullable = false)
    private String shopOrderPhone;

    @Column(name = "SHOP_ORDER_ADDRESS")
    private String shopOrderAddress;

    @Column(name = "SHOP_ORDER_PICKUP_TIME")
    private LocalDateTime shopOrderPickupTime;

    @Column(name = "SHOP_ORDER_NOTE")
    private String shopOrderNote;

    public Integer getShopOrderId() { return shopOrderId; }
    public void setShopOrderId(Integer shopOrderId) { this.shopOrderId = shopOrderId; }

    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }

    public LocalDateTime getShopOrderTime() { return shopOrderTime; }
    public void setShopOrderTime(LocalDateTime shopOrderTime) { this.shopOrderTime = shopOrderTime; }

    public Integer getShopOrderTotal() { return shopOrderTotal; }
    public void setShopOrderTotal(Integer shopOrderTotal) { this.shopOrderTotal = shopOrderTotal; }

    public Byte getShopOrderPickup() { return shopOrderPickup; }
    public void setShopOrderPickup(Byte shopOrderPickup) { this.shopOrderPickup = shopOrderPickup; }

    public Byte getShopPaymentStatus() { return shopPaymentStatus; }
    public void setShopPaymentStatus(Byte shopPaymentStatus) { this.shopPaymentStatus = shopPaymentStatus; }

    public Byte getShopOrderStatus() { return shopOrderStatus; }
    public void setShopOrderStatus(Byte shopOrderStatus) { this.shopOrderStatus = shopOrderStatus; }

    public String getShopOrderName() { return shopOrderName; }
    public void setShopOrderName(String shopOrderName) { this.shopOrderName = shopOrderName; }

    public String getShopOrderPhone() { return shopOrderPhone; }
    public void setShopOrderPhone(String shopOrderPhone) { this.shopOrderPhone = shopOrderPhone; }

    public String getShopOrderAddress() { return shopOrderAddress; }
    public void setShopOrderAddress(String shopOrderAddress) { this.shopOrderAddress = shopOrderAddress; }

    public LocalDateTime getShopOrderPickupTime() { return shopOrderPickupTime; }
    public void setShopOrderPickupTime(LocalDateTime shopOrderPickupTime) { this.shopOrderPickupTime = shopOrderPickupTime; }

    public String getShopOrderNote() { return shopOrderNote; }
    public void setShopOrderNote(String shopOrderNote) { this.shopOrderNote = shopOrderNote; }
}