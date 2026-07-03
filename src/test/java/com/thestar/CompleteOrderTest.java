package com.example.thestar1;

import com.example.thestar1.order.dto.CreateRoomOrderDTO;
import com.example.thestar1.order.entity.OrderVO;
import com.example.thestar1.order.repository.OrderRepository;
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
public class CompleteOrderTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void 完成訂單_狀態應該變2() {
        // 1. 建單(PENDING)
        CreateRoomOrderDTO dto = new CreateRoomOrderDTO();
        dto.setCheckInDate(LocalDate.of(2026, 8, 3));
        dto.setCheckOutDate(LocalDate.of(2026, 8, 5));
        CreateRoomOrderDTO.RoomItem item = new CreateRoomOrderDTO.RoomItem();
        item.setRoomTypeId(1);
        item.setQty(1);
        dto.setRooms(List.of(item));
        OrderVO created = orderService.createOrder(1, dto);
        String mtn = created.getMerchantTradeNo();
        orderService.confirmOrder(mtn, created.getTotalAmount(), (byte) 0, "ECPAY123");
        orderService.completeOrder(created.getOrderId());
        entityManager.clear();
        OrderVO vo = orderRepository.findById(created.getOrderId()).orElseThrow();
        assertEquals((byte)2,vo.getOrderStatus());
    }
}
