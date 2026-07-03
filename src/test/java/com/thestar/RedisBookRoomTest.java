package com.example.thestar1;


import com.example.thestar1.room.repository.RoomInventoryRepository;
import com.example.thestar1.order.service.OrderService;
import com.example.thestar1.room.service.RedisRoomStock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisBookRoomTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RoomInventoryRepository roomInventoryRepository;
    @Autowired
    private RedisRoomStock stock;

    @Test
    @Transactional
        // 只回滾 DB，Redis 不受影響
    void redis預扣_扣到剛好與不夠() {
        Integer roomTypeId = 1 /* DB 有的房型 id */;
        LocalDate date = LocalDate.of(2026, 12, 31);   // 用沒人訂過的未來日，避免干擾
        String key = "room:" + roomTypeId + ":" + date;

        redisTemplate.delete(key);   // 先清，確保從乾淨狀態開始

        roomInventoryRepository.initInventory(date, roomTypeId);   // 確保 DB 有這格
        stock.initRedisRoom(roomTypeId, date);              // 種值進 Redis

        int qty = Integer.parseInt(redisTemplate.opsForValue().get(key));

        // 扣到剛好全部 → true，Redis 變 0
        assertTrue(stock.bookRedisRoom(roomTypeId, date, qty));
        assertEquals("0", redisTemplate.opsForValue().get(key));

        // 再扣 1 → false，且補回去後還是 0
        assertFalse(stock.bookRedisRoom(roomTypeId, date, 1));
        assertEquals("0", redisTemplate.opsForValue().get(key));

        redisTemplate.delete(key);   // 收尾清掉
    }
}