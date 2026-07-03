package com.example.thestar1.room.repository;

import com.example.thestar1.room.entity.RoomTypeVO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomTypeRepository extends JpaRepository<RoomTypeVO, Integer> {
}