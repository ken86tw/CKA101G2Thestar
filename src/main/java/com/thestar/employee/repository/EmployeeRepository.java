package com.thestar.employee.repository;

import com.thestar.employee.entity.EmployeeVO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<EmployeeVO, Integer> {

    Optional<EmployeeVO> findByEmployeeMail(String employeeMail);

    boolean existsByEmployeeMail(String employeeMail);
}
