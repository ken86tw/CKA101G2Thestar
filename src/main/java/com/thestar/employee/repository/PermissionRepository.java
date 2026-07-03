package com.example.thestar1.employee.repository;

import com.example.thestar1.employee.entity.PermissionVO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<PermissionVO, Integer> {
}
