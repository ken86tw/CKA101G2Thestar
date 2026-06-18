package com.example.thestar1;

import com.example.thestar1.order.dto.CreateRoomOrderDTO;
import com.example.thestar1.order.entity.OrderVO;
import com.example.thestar1.order.repository.OrderRepository;
import com.example.thestar1.refund.repository.RefundListRepository;
import com.example.thestar1.order.service.OrderService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class CancelOrderTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private RefundListRepository refundListRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void 主動取消已付款訂單_應該取消並建退款記錄() {
        // 1. 建單(PENDING)
        CreateRoomOrderDTO dto = new CreateRoomOrderDTO();
        dto.setCheckInDate(LocalDate.of(2026, 8, 1));
        dto.setCheckOutDate(LocalDate.of(2026, 8, 3));
        CreateRoomOrderDTO.RoomItem item = new CreateRoomOrderDTO.RoomItem();
        item.setRoomTypeId(1);
        item.setQty(1);
        dto.setRooms(List.of(item));

        OrderVO created = orderService.createOrder(1, dto);
        Integer orderId = created.getOrderId();
        String mtn = created.getMerchantTradeNo();
        Integer total = created.getTotalAmount();

        // 2. 付款(PENDING → CONFIRMED)
        orderService.confirmOrder(mtn, total, (byte) 0, "ECPAY123");

        // 3. 主動取消
        orderService.cancelOrder(1,orderId, "測試取消");

        // 4. 清快取後驗證
        entityManager.clear();

        OrderVO after = orderRepository.findById(orderId).orElseThrow();
        assertEquals((byte) 3, after.getOrderStatus());   // 訂單變 CANCELED
    }
}