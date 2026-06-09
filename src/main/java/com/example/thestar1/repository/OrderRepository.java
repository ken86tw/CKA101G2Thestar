package com.example.thestar1.repository;
import com.example.thestar1.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderVO,Integer> {


    @Modifying
    @Query(value = "UPDATE ROOM_ORDER SET ORDER_STATUS = 1 , PAID_AMOUNT = :paidAmount, PAYMENT_METHOD = :paymentMethod," +
            "ECPAY_TRADE_NO = :ecpayTradeNo WHERE MERCHANT_TRADE_NO = :merchantTradeNo AND ORDER_STATUS = 0",nativeQuery = true)
    int completeOrderPayment(@Param("paidAmount") Integer paidAmount,
                     @Param("paymentMethod")Byte paymentMethod,
                     @Param("merchantTradeNo")String merchantTradeNo,
                     @Param("ecpayTradeNo")String ecpayTradeNo);

    List<OrderVO> findByOrderStatusAndCreatedTimeBefore(Byte orderStatus, LocalDateTime time);


    @Modifying
    @Query(value = "UPDATE ROOM_ORDER SET ORDER_STATUS = 3 WHERE ORDER_ID = :orderId AND ORDER_STATUS = 0",nativeQuery = true)
    int canceledOrderPayment(@Param("orderId")Integer orderId);
}
