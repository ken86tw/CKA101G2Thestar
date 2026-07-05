package com.thestar.employee.dto;

import com.thestar.employee.entity.PermissionVO;

public class PermissionDTO {

    private Integer permissionId;
    private String permissionName;
    private String permissionCode;
    private Byte status;

    public static PermissionDTO from(PermissionVO permission) {
        PermissionDTO dto = new PermissionDTO();
        dto.permissionId = permission.getPermissionId();
        dto.permissionName = permission.getPermissionName();
        dto.permissionCode = permission.getPermissionCode();
        dto.status = permission.getStatus();
        return dto;
    }

    public Integer getPermissionId() {
        return permissionId;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public Byte getStatus() {
        return status;
    }
}
