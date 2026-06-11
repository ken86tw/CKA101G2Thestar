package com.example.thestar1;

import com.example.thestar1.dto.CheckInDTO;
import com.example.thestar1.entity.OrderListVO;
import com.example.thestar1.entity.RoomVO;
import com.example.thestar1.repository.OrderListRepository;
import com.example.thestar1.repository.RoomRepository;
import com.example.thestar1.repository.StayRecordRepository;
import com.example.thestar1.service.StayRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class StayRecordServiceTest {

    @Autowired
    private StayRecordService stayRecordService;
    @Autowired
    private OrderListRepository orderListRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private StayRecordRepository stayRecordRepository;

    @Test
    void checkIn_success() {
        Integer orderListId = 1;     // 雙人房 x2、訂單 CONFIRMED
        Integer roomId = 101;        // 雙人房、空閒
        String stayCustomer = "測試住客";
        Integer employeeId = 3;
        CheckInDTO dto = new CheckInDTO();
        dto.setRoomId(roomId);
        dto.setStayCustomer(stayCustomer);
        dto.setOrderListId(orderListId);
        // 配房前先記下這筆明細已有幾筆住宿紀錄
        OrderListVO orderList = orderListRepository.findById(orderListId).orElseThrow();
        long before = stayRecordRepository.countByOrderListvo(orderList);

        // 執行 checkIn
        stayRecordService.checkIn(employeeId, dto );

        // 驗證一：住宿紀錄多了一筆
        long after = stayRecordRepository.countByOrderListvo(orderList);
        assertEquals(before + 1, after);

        // 驗證二：那間房變成入住中
        RoomVO room = roomRepository.findByRoomId(roomId);
        assertEquals((byte) 1, room.getRoomStatus());
    }
}