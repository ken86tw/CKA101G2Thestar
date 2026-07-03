package com.thestar.refund.repository;

import com.thestar.refund.entity.RefundListVO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundListRepository extends JpaRepository<RefundListVO,Integer> {
}
