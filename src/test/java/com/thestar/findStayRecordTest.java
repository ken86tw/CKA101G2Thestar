package com.example.thestar1;

import com.example.thestar1.stayrecord.entity.StayRecordVO;
import com.example.thestar1.stayrecord.service.StayRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class findStayRecordTest {

    @Autowired
    private StayRecordService stayRecordService;

    @Test
    void 無條件_應回傳全部且依入住時間遞減() {
        List<StayRecordVO> r = stayRecordService.findStayRecord(null, null, null, null);
        assertFalse(r.isEmpty());
        // checkInTime 由新到舊：前一筆不該早於後一筆
        for (int i = 1; i < r.size(); i++) {
            assertFalse(r.get(i - 1).getCheckInTime().isBefore(r.get(i).getCheckInTime()));
        }
    }

    @Test
    void 依房號201_應只回JOHN() {
        List<StayRecordVO> r = stayRecordService.findStayRecord(201, null, null, null);
        assertEquals(1, r.size());
        assertEquals("JOHN", r.get(0).getStayCustomer());
    }

    @Test
    void 模糊查KIT_應只回KITTY() {
        List<StayRecordVO> r = stayRecordService.findStayRecord(null, "KIT", null, null);
        assertEquals(1, r.size());
        assertEquals("KITTY", r.get(0).getStayCustomer());
    }

    @Test
    void 退房日當天退房_應被涵蓋() {
        // JOHN 7/17 退房；查 7/15~7/17，service 內 end = 7/18 00:00
        // 驗證 < plusDays(1) 有把「退房日當天」算進來
        List<StayRecordVO> r = stayRecordService.findStayRecord(
                null, null, null, LocalDate.of(2026, 7, 17));
        assertTrue(r.stream().anyMatch(s -> "JOHN".equals(s.getStayCustomer())));
    }
}