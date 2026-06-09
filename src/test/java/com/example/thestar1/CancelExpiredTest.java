package com.example.thestar1;

import com.example.thestar1.dto.CreateRoomOrderDTO;
import com.example.thestar1.entity.OrderVO;
import com.example.thestar1.repository.OrderRepository;
import com.example.thestar1.service.OrderService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class CancelExpiredTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private EntityManager entityManager;   // 改時間 + 清快取要用

    @Test
    void 逾時訂單_應該被取消() {
        // 1. 建一筆 PENDING 訂單
        CreateRoomOrderDTO dto = new CreateRoomOrderDTO();
        dto.setCheckInDate(LocalDate.of(2026, 8, 1));
        dto.setCheckOutDate(LocalDate.of(2026, 8, 3));
        CreateRoomOrderDTO.RoomItem room = new CreateRoomOrderDTO.RoomItem();
        room.setRoomTypeId(1);
        room.setQty(1);
        dto.setRooms(List.of(room));

        OrderVO created = orderService.createOrder(1, dto);
        Integer orderId = created.getOrderId();

        // 2. 把 CREATED_TIME 改成 6 分鐘前（騙過逾時判斷）
        entityManager.createNativeQuery(
                        "UPDATE ROOM_ORDER SET CREATED_TIME = ? WHERE ORDER_ID = ?")
                .setParameter(1, LocalDateTime.now().minusMinutes(6))
                .setParameter(2, orderId)
                .executeUpdate();

        // 後半段下一步再給
        // 3. 清快取（剛剛用 native SQL 改了時間，JPA 快取裡是舊的）
        entityManager.clear();

        // 4. 執行逾時取消
        orderService.cancelExpiredOrder();

        // 5. 清快取後重新查（cancelExpiredOrder 也是 native UPDATE，快取要清）
        entityManager.clear();

        // 6. 驗證：狀態應該變成 3（CANCELED）
        OrderVO after = orderRepository.findById(orderId).orElseThrow();
        assertEquals((byte) 3, after.getOrderStatus());
    }
}