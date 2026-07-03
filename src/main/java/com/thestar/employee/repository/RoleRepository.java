package com.thestar.employee.repository;

import com.thestar.employee.entity.RoleVO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleVO, Integer> {
}
