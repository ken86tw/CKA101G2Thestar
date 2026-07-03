package com.thestar.employee.controller;

import com.thestar.employee.dto.AssignPermissionsDTO;
import com.thestar.employee.dto.RoleCreateDTO;
import com.thestar.employee.dto.RoleDTO;
import com.thestar.employee.entity.RoleVO;
import com.thestar.employee.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/thestar/admin/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<List<RoleDTO>> listAll() {
        List<RoleDTO> roles = roleService.findAll().stream().map(RoleDTO::from).collect(Collectors.toList());
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> findOne(@PathVariable Integer id) {
        return ResponseEntity.ok(RoleDTO.from(roleService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<RoleDTO> create(@RequestBody RoleCreateDTO dto) {
        RoleVO role = roleService.create(dto);
        return ResponseEntity.status(201).body(RoleDTO.from(role));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleDTO> update(@PathVariable Integer id, @RequestBody RoleCreateDTO dto) {
        RoleVO role = roleService.update(id, dto);
        return ResponseEntity.ok(RoleDTO.from(role));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RoleDTO> updateStatus(@PathVariable Integer id, @RequestParam boolean enabled) {
        RoleVO role = roleService.updateStatus(id, enabled);
        return ResponseEntity.ok(RoleDTO.from(role));
    }

    @PutMapping("/{id}/permissions")
    public ResponseEntity<RoleDTO> updatePermissions(@PathVariable Integer id, @RequestBody AssignPermissionsDTO dto) {
        RoleVO role = roleService.updatePermissions(id, dto.getPermissionIds());
        return ResponseEntity.ok(RoleDTO.from(role));
    }
}
