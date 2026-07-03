package com.example.thestar1.room.service;


import com.example.thestar1.room.repository.RoomInventoryRepository;
import com.example.thestar1.room.repository.RoomTypeRepository;
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
    //建立redis的key ex: room:2:2026-07-12 以房型加日期作為主鍵
    private String roomKey(Integer roomTypeId, LocalDate date) {
        return "room:" + roomTypeId + ":" + date;
    }


    //初始化redis庫存
    public void initRedisRoom(Integer roomTypeId, LocalDate date) {
        String key = roomKey(roomTypeId, date);
        Integer available = roomInventoryRepository.checkInventory(roomTypeId, date);

        //若查詢時為空值代表尚未被訂房即為滿房
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


    //redis訂房
    public boolean bookRedisRoom(Integer roomTypeId, LocalDate date, int qty) {
        String key = roomKey(roomTypeId, date);
        long result = redisTemplate.opsForValue().decrement(key, qty);

        //若扣除庫存後小於零代表超賣 手動回滾
        if (result < 0) {
            redisTemplate.opsForValue().increment(key, qty);
            return false;
        }
        return true;
    }

    // 歸還庫存  已消失就不再重複建立，留給下次 initRedisRoom 從資料庫重建
    public void releaseRoom(Integer roomTypeId, LocalDate date, int qty) {
        String key = roomKey(roomTypeId, date);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().increment(key, qty);
        }
    }
}
