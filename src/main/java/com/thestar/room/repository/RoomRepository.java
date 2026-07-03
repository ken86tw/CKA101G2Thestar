package com.example.thestar1.room.repository;

import com.example.thestar1.room.entity.RoomVO;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;

public interface RoomRepository extends JpaRepository<RoomVO , Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    RoomVO findByRoomId(Integer roomId);


    List<RoomVO> findByRoomTypeIdOrderByRoomId(Integer roomTypeId);
}
