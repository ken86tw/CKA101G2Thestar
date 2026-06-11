package com.example.thestar1.repository;

import com.example.thestar1.entity.OrderListVO;
import com.example.thestar1.entity.RoomVO;
import com.example.thestar1.entity.StayRecordVO;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface StayRecordRepository extends JpaRepository<StayRecordVO,Integer>{


    int countByOrderListvo(OrderListVO orderListVO);
}
