package com.thestar.room.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.thestar.room.entity.RoomTypeVO;
import com.thestar.room.entity.RoomVO;
import com.thestar.room.repository.RoomRepository;

@Service
@Transactional
public class RoomService {

	@Autowired
	private RoomRepository repository;

	@Autowired
	private RoomTypeService roomTypeService;

	// 查詢所有房間
	public List<RoomVO> findAll() {
		// 先從資料庫撈出所有資料，並指派給變數 list
		List<RoomVO> list = repository.findAll();

		// 確定 list 已經建立，再進行搜尋房型編號
		for (RoomVO room : list) {
			RoomTypeVO type = roomTypeService.getOneRoomType(room.getRoomTypeId());
			if (type != null) {
				room.setRoomTypeName(type.getRoomTypeName());
			}
		}
		return list;
	}

	// 查詢單一房間
	public RoomVO findById(Integer id) {
		RoomVO room = repository.findById(id)
				// 找不到對應id時，回傳錯誤訊息
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到對應ID的房間"));

		// 查出對應的房型名稱並補入
		RoomTypeVO type = roomTypeService.getOneRoomType(room.getRoomTypeId());
		if (type != null) {
			room.setRoomTypeName(type.getRoomTypeName());
		}
		return room;
	}

	
	// 新增或更新房間
	public RoomVO save(RoomVO room) {
		return repository.save(room);
	}

	
	// 刪除房間
	public void deleteById(Integer id) {
		repository.deleteById(id);
	}

	// 檢查單一房型ID是否存在
	public boolean existsById(Integer roomId) {
		// 這裡直接呼叫 repository 內建的方法
		return repository.existsById(roomId);
	}
}
