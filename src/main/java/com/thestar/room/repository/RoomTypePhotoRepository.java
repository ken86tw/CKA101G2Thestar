package com.thestar.room.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thestar.room.entity.RoomTypePhotoVO;

public interface RoomTypePhotoRepository extends JpaRepository<RoomTypePhotoVO, Integer> {
	
    List<RoomTypePhotoVO> findByRoomTypeVORoomTypeId(Integer roomTypeId);

    // Spring Data JPA 會自動解析並刪除對應的資料
    void deleteByRoomTypeVORoomTypeId(Integer roomTypeId);
}