package com.example.thestar1.order.repository;

import com.example.thestar1.order.entity.OrderListVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderListRepository extends JpaRepository<OrderListVO,Integer> {

    //加總一張訂單的所有住宿明細裡面的房間數量
    @Query(value = "SELECT SUM(QUANTITY) FROM ROOM_ORDER_LIST WHERE ORDER_ID = :orderId ",nativeQuery = true)
    int sumQtyByOrderId(@Param("orderId") Integer orderId);

    List<OrderListVO>  findByOrdervoOrderId(Integer orderId);
}
