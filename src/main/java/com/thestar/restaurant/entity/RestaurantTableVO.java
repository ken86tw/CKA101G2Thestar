package com.thestar.restaurant.entity;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
 * 注意：TABLE 是 SQL 的保留字，若在某些資料庫出現錯誤，
 * 可改用 @Table(name = "`TABLE_TYPE`")
 */
@Entity
@Table(name = "TABLE_TYPE")
public class RestaurantTableVO implements Serializable {
    private static final long serialVersionUID = 1L;

    // 保持屬性（欄位）乾淨
    private Integer tableTypeId;
    private String tableTypeName;
    private Integer tableTypeCount;

    // =========================================================================
    // 所有的註解都集中在 Getter 方法上
    // =========================================================================

    // --- TABLE_TYPE_ID (PK, AI) ---
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TABLE_TYPE_ID")
    public Integer getTableTypeId() {
        return tableTypeId;
    }

    public void setTableTypeId(Integer tableTypeId) {
        this.tableTypeId = tableTypeId;
    }

    // --- TABLE_TYPE_NAME ---
    @Column(name = "TABLE_TYPE_NAME")
    @NotBlank(message = "桌型名稱：請勿空白") // 驗證註解也一併放在 Getter 上
    public String getTableTypeName() {
        return tableTypeName;
    }

    public void setTableTypeName(String tableTypeName) {
        this.tableTypeName = tableTypeName;
    }

    // --- TABLE_TYPE_COUNT ---
    @Column(name = "TABLE_TYPE_COUNT")
    @NotNull(message = "桌型數目：請勿空白")
    public Integer getTableTypeCount() {
        return tableTypeCount;
    }

    public void setTableTypeCount(Integer tableTypeCount) {
        this.tableTypeCount = tableTypeCount;
    }
}