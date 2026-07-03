package com.example.thestar1.employee.repository;

import com.example.thestar1.employee.entity.RoleVO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleVO, Integer> {
}
