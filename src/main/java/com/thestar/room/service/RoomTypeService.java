package com.thestar.room.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.thestar.room.entity.RoomTypeVO;
import com.thestar.room.repository.RoomTypePhotoRepository;
import com.thestar.room.repository.RoomTypeRepository;

@Service // 標記為 Service 物件，讓 Spring 管理
@Transactional
public class RoomTypeService {

	@Autowired // 自動注入
	private RoomTypeRepository repository;

	@Autowired
	private RoomTypePhotoRepository photoRepository;

	// 查詢所有房型
	public List<RoomTypeVO> getAllRoomTypes() {
		return repository.findAll();
	}

	// 查詢單一房型
	public RoomTypeVO getOneRoomType(Integer id) {
		return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到對應的房型")); // 找不到對應id時，回傳錯誤訊息
	}

	// 新增房型
	@Transactional
	public RoomTypeVO addRoomType(RoomTypeVO roomType) {
		return repository.save(roomType);
	}

	// 更新房型
	@Transactional
	public void updateRoomType(RoomTypeVO roomType) {
		System.out.println("Service層收到的ID: " + roomType.getRoomTypeId());
		repository.save(roomType);
		System.out.println("Service層已執行save操作");
	}

	// 刪除房型
	@Transactional
	public void deleteRoomType(Integer id) {
		// 1. 先根據 room_type_id 刪除所有相關聯的照片
		photoRepository.deleteByRoomTypeVORoomTypeId(id);

		// 2. 再刪除房型本身
		repository.deleteById(id);

	}
}
