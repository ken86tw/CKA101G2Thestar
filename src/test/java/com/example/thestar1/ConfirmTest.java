package com.example.thestar1;

import com.example.thestar1.order.dto.CreateRoomOrderDTO;
import com.example.thestar1.order.entity.OrderVO;
import com.example.thestar1.order.repository.OrderRepository;
import com.example.thestar1.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class ConfirmTest {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderService orderService;
    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    void testConfirmOrder() {
        Integer paidAmount = 10000;
        Byte paymentMethod = (byte) 0;
        CreateRoomOrderDTO dto = new CreateRoomOrderDTO();
        dto.setCheckInDate(LocalDate.of(2026, 8, 1));
        dto.setCheckOutDate(LocalDate.of(2026, 8, 3));
        CreateRoomOrderDTO.RoomItem rit = new CreateRoomOrderDTO.RoomItem();
        rit.setRoomTypeId(1);
        rit.setQty(1);
        dto.setRooms(List.of(rit));

        OrderVO created = orderService.createOrder(1, dto);
        String mtn = created.getMerchantTradeNo();

        orderService.confirmOrder(mtn, paidAmount, paymentMethod, "ECPAY123");
        entityManager.clear();
        OrderVO after = orderRepository.findById(created.getOrderId()).orElseThrow();
        assertEquals((byte) 1, after.getOrderStatus());


    }
}
