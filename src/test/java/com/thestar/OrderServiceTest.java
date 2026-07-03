package com.example.thestar1;

import com.example.thestar1.order.dto.CreateRoomOrderDTO;
import com.example.thestar1.order.entity.OrderVO;
import com.example.thestar1.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional           // 每個測試跑完自動回滾
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Test
    void createorder() {
        // 準備一筆訂房:雙人房(房型1)訂 2 間,住 2 晚
        CreateRoomOrderDTO dto = new CreateRoomOrderDTO();
        dto.setCheckInDate(LocalDate.of(2026, 8, 1));
        dto.setCheckOutDate(LocalDate.of(2026, 8, 3));   // 8/1、8/2 兩晚

        CreateRoomOrderDTO.RoomItem room = new CreateRoomOrderDTO.RoomItem();
        room.setRoomTypeId(1);
        room.setQty(2);
        dto.setRooms(List.of(room));

        // 執行
        OrderVO order = orderService.createOrder(1, dto);// 驗證
        assertNotNull(order.getOrderId());              // 訂單有建起來(拿到 PK)
        assertEquals(1, order.getMemberId());          // memberId 正確
        assertEquals((byte) 0, order.getOrderStatus()); // PENDING
        // 雙人房單價 4000 × 2 間 × 2 晚 = 16000
        assertEquals(16000, order.getTotalAmount());
        assertEquals(1, order.getOrderList().size());   // 一筆明細
    }
}