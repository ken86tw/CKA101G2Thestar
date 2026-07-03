package com.thestar.employee.repository;

import com.thestar.employee.entity.JobTitleVO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobTitleRepository extends JpaRepository<JobTitleVO, Integer> {
}
