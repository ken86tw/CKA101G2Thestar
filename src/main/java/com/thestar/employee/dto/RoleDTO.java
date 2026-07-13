package com.thestar.employee.dto;

import com.thestar.employee.entity.RoleVO;

import java.util.List;
import java.util.stream.Collectors;

public class RoleDTO {

    private Integer roleId;
    private String roleName;
    private String roleCode;
    private Byte status;
    private List<PermissionDTO> permissions;

    public static RoleDTO from(RoleVO role) {
        RoleDTO dto = new RoleDTO();
        dto.roleId = role.getRoleId();
        dto.roleName = role.getRoleName();
        dto.roleCode = role.getRoleCode();
        dto.status = role.getStatus();
        dto.permissions = role.getPermissions().stream().map(PermissionDTO::from).collect(Collectors.toList());
        return dto;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public Byte getStatus() {
        return status;
    }

    public List<PermissionDTO> getPermissions() {
        return permissions;
    }
}
