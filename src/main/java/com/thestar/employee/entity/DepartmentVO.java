package com.thestar.employee.entity;

import jakarta.persistence.*;

/**
 * 唯讀查詢用途（部門下拉選單、列表顯示部門名稱），不提供部門本身的新增/修改/刪除。
 */
@Entity
@Table(name = "DEPARTMENT")
public class DepartmentVO {

    @Id
    @Column(name = "DEPARTMENT_ID", updatable = false)
    private Integer departmentId;

    @Column(name = "DEPARTMENT_NAME")
    private String departmentName;

    @Column(name = "STATUS")
    private Byte status;

    public Integer getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public Byte getStatus() {
        return status;
    }
}
