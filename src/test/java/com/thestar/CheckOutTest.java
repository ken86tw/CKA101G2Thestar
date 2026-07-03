package com.example.thestar1;

import com.example.thestar1.stayrecord.dto.CheckInDTO;
import com.example.thestar1.order.entity.OrderListVO;
import com.example.thestar1.stayrecord.entity.StayRecordVO;
import com.example.thestar1.order.repository.OrderListRepository;
import com.example.thestar1.order.repository.OrderRepository;
import com.example.thestar1.room.repository.RoomRepository;
import com.example.thestar1.stayrecord.service.StayRecordService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class CheckOutTest {

    @Autowired
    private StayRecordService stayRecordService;
    @Autowired
    private OrderListRepository orderListRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private EntityManager entityManager;

    private final Integer orderListId = 1;   // 雙人房 x2、訂單 CONFIRMED
    private final Integer roomA = 101;
    private final Integer roomB = 102;
    private final Integer employeeId = 3;

    private void checkIn(Integer roomId) {
        CheckInDTO dto = new CheckInDTO();
        dto.setOrderListId(orderListId);
        dto.setRoomId(roomId);
        dto.setStayCustomer("測試住客");
        stayRecordService.checkIn(employeeId, dto);
    }

    private Integer orderId() {
        OrderListVO orderList = orderListRepository.findById(orderListId).orElseThrow();
        return orderList.getOrdervo().getOrderId();
    }

    @Test
    void checkOut_部分退房_訂單未完成() {
        checkIn(roomA);   // 訂兩間只入住一間

        StayRecordVO stay = stayRecordService.checkOut(roomA, employeeId);

        assertNotNull(stay.getCheckOutTime());
        assertEquals(employeeId, stay.getCheckOutEmployeeId());
        assertEquals((byte) 0, roomRepository.findById(roomA).orElseThrow().getRoomStatus());
        assertEquals((byte) 1, orderRepository.findById(orderId()).orElseThrow().getOrderStatus());
    }

    @Test
    void checkOut_全部退房_訂單自動完成() {
        checkIn(roomA);
        checkIn(roomB);

        stayRecordService.checkOut(roomA, employeeId);
        assertEquals((byte) 1, orderRepository.findById(orderId()).orElseThrow().getOrderStatus());
        entityManager.clear();
        stayRecordService.checkOut(roomB, employeeId);
        assertEquals((byte) 2, orderRepository.findById(orderId()).orElseThrow().getOrderStatus());
    }

    @Test
    void checkOut_重複退房_丟例外() {
        checkIn(roomA);
        stayRecordService.checkOut(roomA, employeeId);

        assertThrows(IllegalStateException.class,
                () -> stayRecordService.checkOut(roomA, employeeId));
    }
}