package com.thestar.employee.service;

import com.thestar.employee.dto.RoleCreateDTO;
import com.thestar.employee.entity.PermissionVO;
import com.thestar.employee.entity.RoleVO;
import com.thestar.employee.repository.PermissionRepository;
import com.thestar.employee.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleVO> findAll() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public RoleVO findById(Integer roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new NoSuchElementException("查無此角色"));
    }

    @Transactional(readOnly = true)
    public List<PermissionVO> findAllPermissions() {
        return permissionRepository.findAll();
    }

    public RoleVO create(RoleCreateDTO dto) {
        if (dto.getRoleCode() == null || dto.getRoleCode().isBlank()) {
            throw new IllegalArgumentException("角色代碼為必填");
        }
        if (!dto.getRoleCode().startsWith("ROLE_")) {
            throw new IllegalArgumentException("角色代碼需以 ROLE_ 開頭");
        }
        RoleVO role = new RoleVO();
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setStatus((byte) 1);
        return roleRepository.save(role);
    }

    public RoleVO update(Integer roleId, RoleCreateDTO dto) {
        RoleVO role = findById(roleId);
        role.setRoleName(dto.getRoleName());
        return role;
    }

    public RoleVO updateStatus(Integer roleId, boolean enabled) {
        RoleVO role = findById(roleId);
        role.setStatus((byte) (enabled ? 1 : 0));
        return role;
    }

    public RoleVO updatePermissions(Integer roleId, List<Integer> permissionIds) {
        RoleVO role = findById(roleId);
        Set<PermissionVO> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
        role.setPermissions(permissions);
        return role;
    }
}
