package com.example.thestar1;

import com.example.thestar1.room.repository.RoomInventoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional    // 關鍵：測完自動 rollback，DB 不留資料
public class RoomInventoryRepositoryTest {

    @Autowired
    private RoomInventoryRepository inventoryRepo;

    @Test   // 標記這是一個測試方法
    void testBookRooms() {
        LocalDate date = LocalDate.of(2026, 7, 1);
        Integer roomTypeId = 1;   // ← 改成 DB 裡真有的房型 ID

        inventoryRepo.initInventory(date, roomTypeId);

        int a1 = inventoryRepo.bookRooms(date, roomTypeId, 1);
        assertEquals(1, a1);      // 正常扣，預期影響 1 列

        int a2 = inventoryRepo.bookRooms(date, roomTypeId, 99999);
        assertEquals(0, a2);      // 超量，預期影響 0 列（被守門擋下）
    }
}