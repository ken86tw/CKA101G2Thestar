package com.example.thestar1.content.repository;

import com.example.thestar1.content.entity.ReviewVO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<ReviewVO, Integer> {
}
