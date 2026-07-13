package com.thestar.stayrecord.repository;

import com.thestar.order.entity.OrderListVO;
import com.thestar.order.entity.OrderVO;
import com.thestar.stayrecord.entity.StayRecordVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    //退房時使用列出所有還沒退房的房間
    List<StayRecordVO> findByCheckOutTimeIsNullOrderByOrderListvoOrdervoOrderIdDesc();


    //查詢詳情時查詢訂單
    @Query("SELECT o FROM StayRecordVO s JOIN s.orderListvo ol JOIN ol.ordervo o WHERE s.stayId = :stayId")
    OrderVO findOrderByStayId(@Param("stayId") Integer stayId);

}


