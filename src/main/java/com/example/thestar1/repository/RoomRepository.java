package com.example.thestar1.repository;

import com.example.thestar1.entity.RoomVO;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface RoomRepository extends JpaRepository<RoomVO , Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    RoomVO findByRoomId(Integer roomId);
}
