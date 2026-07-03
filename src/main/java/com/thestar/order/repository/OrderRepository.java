package com.thestar.order.repository;

import com.thestar.order.entity.OrderVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

//用的是hibernate捷徑函數
public interface OrderRepository extends JpaRepository<OrderVO, Integer> {

    //自訂sql第一為了防併發 第二需要條件過濾
    //付款後訂單轉為確認
    @Modifying
    @Query(value = "UPDATE ROOM_ORDER SET ORDER_STATUS = 1 , PAID_AMOUNT = :paidAmount, PAYMENT_METHOD = :paymentMethod," +
            "ECPAY_TRADE_NO = :ecpayTradeNo WHERE MERCHANT_TRADE_NO = :merchantTradeNo AND ORDER_STATUS = 0 AND :paidAmount = TOTAL_AMOUNT - DISCOUNT_AMOUNT", nativeQuery = true)
    int confirmOrderPayment(@Param("paidAmount") Integer paidAmount,
                            @Param("paymentMethod") Byte paymentMethod,
                            @Param("merchantTradeNo") String merchantTradeNo,
                            @Param("ecpayTradeNo") String ecpayTradeNo);

    List<OrderVO> findByOrderStatusAndCreatedTimeBefore(Byte orderStatus, LocalDateTime time);

    //定時取消超時訂單
    @Modifying
    @Query(value = "UPDATE ROOM_ORDER SET ORDER_STATUS = 3 WHERE ORDER_ID = :orderId AND ORDER_STATUS = 0", nativeQuery = true)
    int canceledOrderPayment(@Param("orderId") Integer orderId);


    //退房後完成訂單
    @Modifying
    @Query(value = "UPDATE ROOM_ORDER SET ORDER_STATUS = 2 WHERE ORDER_ID = :orderId AND ORDER_STATUS = 1", nativeQuery = true)
    int finishOrder(@Param("orderId") Integer orderId);

    //會員取消訂單
    @Modifying
    @Query(value = "UPDATE ROOM_ORDER  SET ORDER_STATUS = 3 WHERE ORDER_ID = :orderId AND ORDER_STATUS = 1  ", nativeQuery = true)
    int customerCancelOrder(@Param("orderId") Integer orderId);

    //供會員查詢並利用訂單狀態區分會員的訂單
    Page<OrderVO> findByMemberIdAndOrderStatus(Integer MemberId, Byte OrderStatus, Pageable pageable);

    //會員查詢時確認查詢的訂單與會員皆為同一人
    boolean existsByMemberIdAndOrderId(Integer memberId, Integer orderId);

    //後台人員用查詢訂單
    Page<OrderVO> findByOrderStatus(Byte orderStatus, Pageable pageable);


    //入住時查詢所有訂單按鈕
    @Query("SELECT o FROM OrderVO o WHERE o.orderStatus = 1 " +
            "AND NOT EXISTS (SELECT s FROM StayRecordVO s WHERE s.orderListvo.ordervo = o) " +
            "ORDER BY o.orderId")
    List<OrderVO> findNotCheckInOrders();
}
