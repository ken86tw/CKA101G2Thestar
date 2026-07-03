package com.example.thestar1.employee.service;

import com.example.thestar1.employee.dto.EmployeeCreateDTO;
import com.example.thestar1.employee.dto.EmployeeUpdateDTO;
import com.example.thestar1.employee.entity.EmployeeVO;
import com.example.thestar1.employee.entity.RoleVO;
import com.example.thestar1.employee.repository.EmployeeRepository;
import com.example.thestar1.employee.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository, RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<EmployeeVO> findAll() {
        return employeeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public EmployeeVO findById(Integer employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NoSuchElementException("查無此員工"));
    }

    public EmployeeVO create(EmployeeCreateDTO dto) {
        if (dto.getEmployeeMail() == null || dto.getEmployeeMail().isBlank()) {
            throw new IllegalArgumentException("Email 為必填");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("密碼為必填");
        }
        if (employeeRepository.existsByEmployeeMail(dto.getEmployeeMail())) {
            throw new IllegalStateException("此 Email 已被使用");
        }

        EmployeeVO employee = new EmployeeVO();
        employee.setDepartmentId(dto.getDepartmentId());
        employee.setEmployeeName(dto.getEmployeeName());
        employee.setEmployeeMail(dto.getEmployeeMail());
        employee.setEmployeePassword(passwordEncoder.encode(dto.getPassword()));
        employee.setPhone(dto.getPhone());
        employee.setAddress(dto.getAddress());
        employee.setJobTitleId(dto.getJobTitleId());
        employee.setGender(dto.getGender());
        employee.setStatus((byte) 1);
        employee.setHireDate(dto.getHireDate());

        return employeeRepository.save(employee);
    }

    public EmployeeVO update(Integer employeeId, EmployeeUpdateDTO dto) {
        EmployeeVO employee = findById(employeeId);
        employee.setDepartmentId(dto.getDepartmentId());
        employee.setEmployeeName(dto.getEmployeeName());
        employee.setPhone(dto.getPhone());
        employee.setAddress(dto.getAddress());
        employee.setJobTitleId(dto.getJobTitleId());
        employee.setGender(dto.getGender());
        return employee;
    }

    public EmployeeVO updateStatus(Integer employeeId, boolean enabled) {
        EmployeeVO employee = findById(employeeId);
        employee.setStatus((byte) (enabled ? 1 : 0));
        return employee;
    }

    public void changePassword(Integer employeeId, String oldPassword, String newPassword) {
        EmployeeVO employee = findById(employeeId);
        if (!passwordEncoder.matches(oldPassword, employee.getEmployeePassword())) {
            throw new IllegalArgumentException("原密碼錯誤");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("新密碼為必填");
        }
        employee.setEmployeePassword(passwordEncoder.encode(newPassword));
    }

    public EmployeeVO assignRoles(Integer employeeId, List<Integer> roleIds) {
        EmployeeVO employee = findById(employeeId);
        Set<RoleVO> roles = new HashSet<>(roleRepository.findAllById(roleIds));
        employee.setRoles(roles);
        return employee;
    }

    public void updateLastLoginTime(Integer employeeId) {
        employeeRepository.findById(employeeId)
                .ifPresent(employee -> employee.setLastLoginTime(LocalDateTime.now()));
    }
}
