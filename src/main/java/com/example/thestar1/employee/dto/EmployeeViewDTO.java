package com.example.thestar1.employee.dto;

import com.example.thestar1.employee.entity.EmployeeVO;
import com.example.thestar1.employee.entity.RoleVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class EmployeeViewDTO {

    private Integer employeeId;
    private Integer departmentId;
    private String employeeName;
    private String employeeMail;
    private String phone;
    private String address;
    private Integer jobTitleId;
    private String gender;
    private Byte status;
    private LocalDate hireDate;
    private LocalDateTime lastLoginTime;
    private List<String> roleCodes;
    private List<String> roleNames;

    public static EmployeeViewDTO from(EmployeeVO employee) {
        EmployeeViewDTO dto = new EmployeeViewDTO();
        dto.employeeId = employee.getEmployeeId();
        dto.departmentId = employee.getDepartmentId();
        dto.employeeName = employee.getEmployeeName();
        dto.employeeMail = employee.getEmployeeMail();
        dto.phone = employee.getPhone();
        dto.address = employee.getAddress();
        dto.jobTitleId = employee.getJobTitleId();
        dto.gender = employee.getGender();
        dto.status = employee.getStatus();
        dto.hireDate = employee.getHireDate();
        dto.lastLoginTime = employee.getLastLoginTime();
        dto.roleCodes = employee.getRoles().stream().map(RoleVO::getRoleCode).collect(Collectors.toList());
        dto.roleNames = employee.getRoles().stream().map(RoleVO::getRoleName).collect(Collectors.toList());
        return dto;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getEmployeeMail() {
        return employeeMail;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public Integer getJobTitleId() {
        return jobTitleId;
    }

    public String getGender() {
        return gender;
    }

    public Byte getStatus() {
        return status;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public List<String> getRoleNames() {
        return roleNames;
    }
}
