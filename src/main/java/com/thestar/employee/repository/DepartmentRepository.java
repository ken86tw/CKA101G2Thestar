package com.thestar.employee.repository;

import com.thestar.employee.entity.DepartmentVO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<DepartmentVO, Integer> {
}
