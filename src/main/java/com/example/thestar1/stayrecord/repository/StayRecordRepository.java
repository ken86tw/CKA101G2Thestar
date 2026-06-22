package com.example.thestar1.stayrecord.repository;

import com.example.thestar1.order.entity.OrderListVO;
import com.example.thestar1.stayrecord.entity.StayRecordVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface StayRecordRepository extends JpaRepository<StayRecordVO, Integer> {


    int countByOrderListvo(OrderListVO orderListVO);


    StayRecordVO findByRoomIdAndCheckOutTimeIsNull(Integer roomId);

    //找出屬於這張訂單的訂單明細的所有住宿紀錄不管有無退房
    //COUNT * FROM STAY_RECORD s JOIN ROOM_ORDER_LIST ol ON s.ORDER_LIST_ID = ol.ORDER_LIST_ID
    //WHERE ol.ORDER_ID = :orderId
    int countByOrderListvoOrdervoOrderId(Integer orderId);

    //找出屬於這張訂單有退房的住宿紀錄
    int countByOrderListvoOrdervoOrderIdAndCheckOutTimeIsNull(Integer orderId);


    //複合查詢住宿紀錄
    @Query("FROM StayRecordVO s WHERE (:roomId IS NULL OR  s.roomId = :roomId ) AND (:stayCustomer IS NULL OR s.stayCustomer LIKE CONCAT('%',:stayCustomer,'%') ) " +
            "AND (:checkInTime is null OR s.checkInTime >= :checkInTime) AND (s.checkOutTime is null OR :checkOutTime is null OR s.checkOutTime < :checkOutTime) ORDER BY s.checkInTime DESC")
    List<StayRecordVO> FrontSearchStayRecordVO(@Param("roomId") Integer roomId,             //確保未退房也查得到
                                               @Param("stayCustomer")String stayCustomer,
                                               @Param("checkInTime") LocalDateTime checkInTime,
                                               @Param("checkOutTime")LocalDateTime checkOutTime);

}


