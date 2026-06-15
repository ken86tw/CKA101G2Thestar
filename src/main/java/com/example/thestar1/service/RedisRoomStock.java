package com.example.thestar1.service;


import com.example.thestar1.repository.RoomInventoryRepository;
import com.example.thestar1.repository.RoomTypeRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class RedisRoomStock {

    private final StringRedisTemplate redisTemplate;
    private final RoomInventoryRepository roomInventoryRepository;
    private final RoomTypeRepository roomTypeRepository;

    public RedisRoomStock(StringRedisTemplate redisTemplate, RoomInventoryRepository roomInventoryRepository, RoomTypeRepository roomTypeRepository) {
        this.redisTemplate = redisTemplate;
        this.roomInventoryRepository = roomInventoryRepository;
        this.roomTypeRepository = roomTypeRepository;
    }

    private String roomKey(Integer roomTypeId, LocalDate date) {
        return "room:" + roomTypeId + ":" + date;
    }

    public void initRedisRoom(Integer roomTypeId, LocalDate date) {
        String key = roomKey(roomTypeId, date);
        Integer available = roomInventoryRepository.checkInventory(roomTypeId, date);

        int room;
        if (available == null) {
            room = roomTypeRepository.findById(roomTypeId).orElseThrow().getRoomTypeAmount();
        } else {
            room = available;
        }

        //當日結束後將redis清除明日重新建立
        Duration ttl = Duration.between(LocalDateTime.now(), date.plusDays(1).atStartOfDay());
        redisTemplate.opsForValue().setIfAbsent(key, String.valueOf(room), ttl);
    }

    public boolean bookRedisRoom(Integer roomTypeId, LocalDate date, int qty) {
        String key = roomKey(roomTypeId, date);
        long result = redisTemplate.opsForValue().decrement(key, qty);

        if (result < 0) {
            redisTemplate.opsForValue().increment(key, qty);
            return false;
        }
        return true;
    }

    // 歸還庫存  已消失就不硬建，留給下次 initRedisRoom 從DB重建
    public void releaseRoom(Integer roomTypeId, LocalDate date, int qty) {
        String key = roomKey(roomTypeId, date);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().increment(key, qty);
        }
    }
}
