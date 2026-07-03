package com.example.thestar1.employee.controller;

import com.example.thestar1.employee.dto.PermissionDTO;
import com.example.thestar1.employee.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/thestar/admin/permission")
public class PermissionController {

    private final RoleService roleService;

    public PermissionController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<List<PermissionDTO>> listAll() {
        List<PermissionDTO> permissions = roleService.findAllPermissions().stream()
                .map(PermissionDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(permissions);
    }
}
