package com.example.thestar1.refund.repository;

import com.example.thestar1.refund.entity.RefundListVO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundListRepository extends JpaRepository<RefundListVO,Integer> {
}
