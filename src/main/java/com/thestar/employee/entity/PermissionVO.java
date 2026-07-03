package com.example.thestar1.employee.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "PERMISSION")
public class PermissionVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PERMISSION_ID", updatable = false)
    private Integer permissionId;

    @Column(name = "PERMISSION_NAME")
    private String permissionName;

    @Column(name = "PERMISSION_CODE")
    private String permissionCode;

    @Column(name = "STATUS")
    private Byte status;

    public PermissionVO() {
        super();
    }

    public Integer getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Integer permissionId) {
        this.permissionId = permissionId;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }
}
