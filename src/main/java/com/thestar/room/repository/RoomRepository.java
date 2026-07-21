package com.thestar.room.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.thestar.room.entity.RoomVO;

import jakarta.persistence.LockModeType;

public interface RoomRepository extends JpaRepository<RoomVO, Integer> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	RoomVO findByRoomId(Integer roomId);

	List<RoomVO> findByRoomTypeIdOrderByRoomId(Integer roomTypeId);

	List<RoomVO> findByRoomTypeIdAndRoomStatusAndRoomSwitchStatus(Integer roomTypeId, Integer roomStatus,
			Boolean roomSwitchStatus);

	long countByRoomTypeId(Integer roomTypeId);

	// 支援房間ID、房型、上下架狀態的複合動態查詢
	@Query("SELECT r FROM RoomVO r WHERE " + "(:roomId IS NULL OR r.roomId = :roomId) AND "
			+ "(:roomTypeId IS NULL OR r.roomTypeId = :roomTypeId) AND "
			+ "(:roomSwitchStatus IS NULL OR r.roomSwitchStatus = :roomSwitchStatus)")
	List<RoomVO> findRoomsByCriteria(@Param("roomId") Integer roomId, @Param("roomTypeId") Integer roomTypeId,
			@Param("roomSwitchStatus") Boolean roomSwitchStatus);

}
