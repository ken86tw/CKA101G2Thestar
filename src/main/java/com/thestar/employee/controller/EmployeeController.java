package com.thestar.employee.controller;

import com.thestar.employee.dto.*;
import com.thestar.employee.entity.EmployeeVO;
import com.thestar.employee.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/thestar/admin/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeViewDTO>> listAll() {
        List<EmployeeViewDTO> employees = employeeService.findAll().stream()
                .map(EmployeeViewDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeViewDTO> findOne(@PathVariable Integer id) {
        EmployeeVO employee = employeeService.findById(id);
        return ResponseEntity.ok(EmployeeViewDTO.from(employee));
    }

    @PostMapping
    public ResponseEntity<EmployeeViewDTO> create(@RequestBody EmployeeCreateDTO dto) {
        EmployeeVO employee = employeeService.create(dto);
        return ResponseEntity.status(201).body(EmployeeViewDTO.from(employee));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeViewDTO> update(@PathVariable Integer id, @RequestBody EmployeeUpdateDTO dto) {
        EmployeeVO employee = employeeService.update(id, dto);
        return ResponseEntity.ok(EmployeeViewDTO.from(employee));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EmployeeViewDTO> updateStatus(@PathVariable Integer id, @RequestParam boolean enabled) {
        EmployeeVO employee = employeeService.updateStatus(id, enabled);
        return ResponseEntity.ok(EmployeeViewDTO.from(employee));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Integer id, @RequestBody ChangePasswordDTO dto) {
        employeeService.changePassword(id, dto.getOldPassword(), dto.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<EmployeeViewDTO> assignRoles(@PathVariable Integer id, @RequestBody AssignRolesDTO dto) {
        EmployeeVO employee = employeeService.assignRoles(id, dto.getRoleIds());
        return ResponseEntity.ok(EmployeeViewDTO.from(employee));
    }
}
