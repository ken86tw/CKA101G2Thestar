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
import com.thestar.stayrecord.entity.StayRecordVO;
import com.thestar.stayrecord.repository.StayRecordRepository;

@Service
@Transactional
public class RoomService {

	@Autowired
	private RoomRepository repository;

	@Autowired
	private RoomTypeService roomTypeService;

	@Autowired
	private StayRecordRepository stayRecordRepository;

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

	// 更新房間
	public RoomVO save(RoomVO room) {
		// 關鍵修改：檢查 roomId 不為 null，且資料庫中確實存在該房間時，才進入更新邏輯
		if (room.getRoomId() != null && repository.existsById(room.getRoomId())) {

			// 從資料庫撈出當前存在的舊資料
			RoomVO existingRoom = repository.findById(room.getRoomId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到房間"));

			// 1. 檢查房間是否處於「已入住」狀態 (STATUS_OCCUPIED = 1)
			if (existingRoom.getRoomStatus() == RoomVO.STATUS_OCCUPIED) {

				// 阻擋上架狀態的變更：比較資料庫中的值與前端傳入的值
				if (!existingRoom.getRoomSwitchStatus().equals(room.getRoomSwitchStatus())) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "該房間目前已入住，無法變更狀態。");
				}

				// 強制保護房間狀態：確保更新時仍維持「已入住」，避免前端傳入其他狀態值
				room.setRoomStatus(existingRoom.getRoomStatus());
			}

			// 若需要確保其他欄位（如 RoomType）不會在更新時變成 null，可在此加入
			if (room.getRoomTypeId() == null) {
				room.setRoomTypeId(existingRoom.getRoomTypeId());
			}
		}

		// 執行儲存
		// 如果是新增，因為不進去上面的 if，這行會直接執行 Insert
		// 如果是更新，因為已經處理好資料，這行會執行 Update
		return repository.save(room);
	}

	// 刪除房間
	public void deleteById(Integer id) {
		RoomVO room = repository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到對應ID的房間"));

		// 檢查是否為「已上架」(這裡以 STATUS_AVAILABLE 為例，請依實際需求調整)
		if (Boolean.TRUE.equals(room.getRoomSwitchStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "刪除失敗：該房間目前為『已上架』狀態，請先下架後再執行刪除。");
		}

		// 檢查歷史紀錄
		List<StayRecordVO> records = stayRecordRepository.findByRoomId(id);
		if (records != null && !records.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "刪除失敗：該房間已有相關住宿紀錄，禁止刪除。");
		}

		repository.deleteById(id);
	}

	// 檢查單一房型ID是否存在
	public boolean existsById(Integer roomId) {
		// 這裡直接呼叫 repository 內建的方法
		return repository.existsById(roomId);
	}

	// 目前在 room 資料表中，有多少間房間屬於指定的房型 (由 roomTypeId 指定)
	// 根據房型編號，統計資料庫中該房型目前已配置的房間總數。
	public long countRoomsByTypeId(Integer roomTypeId) {
		return (int) repository.countByRoomTypeId(roomTypeId);
	}

	public int countBookedRoomsByTypeId(Integer roomTypeId) {
		// 呼叫剛剛新增的 Repository 方法
		return stayRecordRepository.countActiveByRoomTypeId(roomTypeId);
	}
}
