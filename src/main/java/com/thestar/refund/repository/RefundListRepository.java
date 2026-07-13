package com.thestar.refund.repository;

import com.thestar.refund.entity.RefundListVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RefundListRepository extends JpaRepository<RefundListVO, Integer> {


    List<RefundListVO> findByRefundStatusOrderByRefundId(Byte refundStatus);


    @Query(value = "UPDATE REFUND_LIST SET REFUND_STATUS = 1, REFUND_TIME = now(),EMPLOYEE_ID = :employeeId WHERE REFUND_STATUS = 0 AND REFUND_ID = :refundId", nativeQuery = true)
    @Modifying
    int refundOrder(@Param("employeeId") Integer employeeId,
                           @Param("refundId") Integer refundId);
}
