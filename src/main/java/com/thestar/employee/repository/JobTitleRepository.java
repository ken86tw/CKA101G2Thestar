package com.example.thestar1.employee.repository;

import com.example.thestar1.employee.entity.JobTitleVO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobTitleRepository extends JpaRepository<JobTitleVO, Integer> {
}
