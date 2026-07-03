package com.example.thestar1.employee.repository;

import com.example.thestar1.employee.entity.EmployeeVO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<EmployeeVO, Integer> {

    Optional<EmployeeVO> findByEmployeeMail(String employeeMail);

    boolean existsByEmployeeMail(String employeeMail);
}
