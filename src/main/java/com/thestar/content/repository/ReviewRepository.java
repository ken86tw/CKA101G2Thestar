package com.thestar.content.repository;

import com.thestar.content.entity.ReviewVO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<ReviewVO, Integer> {
}
