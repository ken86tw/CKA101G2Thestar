package com.thestar.room.repository;

import com.thestar.room.entity.RoomTypeVO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomTypeRepository extends JpaRepository<RoomTypeVO, Integer> {
}