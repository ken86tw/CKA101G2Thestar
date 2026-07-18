package com.thestar.employee.security;

import com.thestar.employee.entity.EmployeeVO;
import com.thestar.employee.entity.PermissionVO;
import com.thestar.employee.entity.RoleVO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Set;

public class EmployeeUserDetails implements UserDetails {

    private final EmployeeVO employee;

    public EmployeeUserDetails(EmployeeVO employee) {
        this.employee = employee;
    }

    public Integer getEmployeeId() {
        return employee.getEmployeeId();
    }

    public EmployeeVO getEmployee() {
        return employee;
    }

    @Override
    public Set<GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (RoleVO role : employee.getRoles()) {
            if (role.getStatus() == null || role.getStatus() != 1) {
                continue;
            }
            addAuthority(authorities, role.getRoleCode());
            for (PermissionVO permission : role.getPermissions()) {
                if (permission.getStatus() == null || permission.getStatus() != 1) {
                    continue;
                }
                addAuthority(authorities, permission.getPermissionCode());
            }
        }
        return authorities;
    }

    private void addAuthority(Set<GrantedAuthority> authorities, String code) {
        if (code == null || code.isBlank()) {
            return;
        }
        authorities.add(new SimpleGrantedAuthority(code.trim().toUpperCase()));
    }

    @Override
    public String getPassword() {
        return employee.getEmployeePassword();
    }

    @Override
    public String getUsername() {
        return employee.getEmployeeMail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return employee.getStatus() != null && employee.getStatus() == 1;
    }
}
