package com.thestar.restaurant.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "RESTAURANT_MENU")
public class RestaurantMenuVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer itemId;
    private MenuCategoryVO menuCategoryVO;
    private String itemName;
    private String itemDesc;
    private BigDecimal price;
    private String imageUrl;
    private Integer sortOrder;

    @Id
    @Column(name = "ITEM_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getItemId() {
        return itemId;
    }
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    @ManyToOne
    @JoinColumn(name = "CATEGORY_ID")
    @NotNull(message = "菜單分類：請勿空白")
    public MenuCategoryVO getMenuCategoryVO() {
        return menuCategoryVO;
    }
    public void setMenuCategoryVO(MenuCategoryVO menuCategoryVO) {
        this.menuCategoryVO = menuCategoryVO;
    }

    @Column(name = "ITEM_NAME")
    @NotEmpty(message = "餐點名稱：請勿空白")
    @Size(max = 50, message = "餐點名稱：長度不能超過 {max} 字元")
    public String getItemName() {
        return itemName;
    }
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    @Column(name = "ITEM_DESC")
    @Size(max = 200, message = "餐點描述：長度不能超過 {max} 字元")
    public String getItemDesc() {
        return itemDesc;
    }
    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }

    @Column(name = "PRICE", precision = 8, scale = 2)
    @NotNull(message = "餐點價格：請勿空白")
    @DecimalMin(value = "0.01", message = "餐點價格：不能小於 {value}")
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Column(name = "IMAGE_URL")
    @Size(max = 255, message = "圖片路徑：長度不能超過 {max} 字元")
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Column(name = "SORT_ORDER")
    @NotNull(message = "排序：請勿空白")
    public Integer getSortOrder() {
        return sortOrder;
    }
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
