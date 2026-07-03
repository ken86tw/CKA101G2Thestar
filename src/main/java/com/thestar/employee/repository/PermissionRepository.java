package com.thestar.employee.repository;

import com.thestar.employee.entity.PermissionVO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<PermissionVO, Integer> {
}
