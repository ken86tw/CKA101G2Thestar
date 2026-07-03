package com.example.thestar1.employee.security;

import com.example.thestar1.employee.entity.EmployeeVO;
import com.example.thestar1.employee.repository.EmployeeRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class EmployeeUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    public EmployeeUserDetailsService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String employeeMail) throws UsernameNotFoundException {
        EmployeeVO employee = employeeRepository.findByEmployeeMail(employeeMail)
                .orElseThrow(() -> new UsernameNotFoundException("帳號或密碼錯誤"));
        return new EmployeeUserDetails(employee);
    }
}
