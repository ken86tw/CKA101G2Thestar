package com.thestar.employee.entity;

import jakarta.persistence.*;

/**
 * 唯讀查詢用途（職稱下拉選單、列表顯示職稱名稱），不提供職稱本身的新增/修改/刪除。
 */
@Entity
@Table(name = "JOB_TITLE")
public class JobTitleVO {

    @Id
    @Column(name = "JOB_TITLE_ID", updatable = false)
    private Integer jobTitleId;

    @Column(name = "JOB_TITLE_NAME")
    private String jobTitleName;

    @Column(name = "STATUS")
    private Byte status;

    public Integer getJobTitleId() {
        return jobTitleId;
    }

    public String getJobTitleName() {
        return jobTitleName;
    }

    public Byte getStatus() {
        return status;
    }
}
