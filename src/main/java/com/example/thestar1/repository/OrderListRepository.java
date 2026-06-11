package com.example.thestar1.repository;

import com.example.thestar1.entity.OrderListVO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderListRepository extends JpaRepository<OrderListVO,Integer> {
}
