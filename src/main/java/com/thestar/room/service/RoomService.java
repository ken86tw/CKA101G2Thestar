package com.thestar.room.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.thestar.room.entity.RoomTypeVO;
import com.thestar.room.entity.RoomVO;
import com.thestar.room.repository.RoomInventoryRepository;
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

	@Autowired
	private RoomInventoryRepository roomInventoryRepository;

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
		if (room.getRoomId() != null && repository.existsById(room.getRoomId())) {

			// 從資料庫撈出當前存在的舊資料
			RoomVO existingRoom = repository.findById(room.getRoomId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到房間"));

			// 💡 [新增防護 1] 如果試圖在修改時「變更房型 (roomTypeId)」
			if (!existingRoom.getRoomTypeId().equals(room.getRoomTypeId())) {
				// 檢查該房間在今天之後是否有任何未來的住宿訂單紀錄
				int futureRecords = stayRecordRepository.countActiveByRoomTypeId(existingRoom.getRoomTypeId()); // 或針對該特定
																												// room
																												// 檢查未來訂單
				if (futureRecords > 0) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "修改失敗：該房間所屬房型近期或未來已有相關訂單紀錄，禁止變更！");
				}

				// 同時也可以檢查未來庫存表中，是否已經產生了對應日期的排程
				boolean hasFutureInventory = roomInventoryRepository
						.existsByRoomTypeIdAndDate(existingRoom.getRoomTypeId(), LocalDate.now());
				if (hasFutureInventory) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "修改失敗：系統已有該房型未來庫存排程，不可更改房間所屬房型。");
				}
			}

			// 2. 檢查房間是否處於「已入住」狀態 (STATUS_OCCUPIED = 1)
			if (existingRoom.getRoomStatus() == RoomVO.STATUS_OCCUPIED) {
				if (!existingRoom.getRoomSwitchStatus().equals(room.getRoomSwitchStatus())) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "該房間目前已入住，無法變更狀態。");
				}
				room.setRoomStatus(existingRoom.getRoomStatus());
			}

			if (room.getRoomTypeId() == null) {
				room.setRoomTypeId(existingRoom.getRoomTypeId());
			}
		}

		return repository.save(room);
	}

	// 刪除房間
	public void deleteById(Integer id) {
		RoomVO room = repository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到對應ID的房間"));

		// 1. 檢查是否為「已上架」
		if (Boolean.TRUE.equals(room.getRoomSwitchStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "刪除失敗：該房間目前為『已上架』，請下架後再執行刪除。");
		}

		// 2. 檢查歷史住宿紀錄
		List<StayRecordVO> records = stayRecordRepository.findByRoomId(id);
		if (records != null && !records.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "刪除失敗：該房間已有相關住宿紀錄，禁止刪除。");
		}

		// 3. 💡 [新增防護] 檢查該房型是否有影響到未來的庫存排程 (若刪除實體房間，總數會少一個)
		boolean hasFutureInventory = roomInventoryRepository.existsByRoomTypeIdAndDate(room.getRoomTypeId(),
				LocalDate.now());
		if (hasFutureInventory) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "刪除失敗：該房間所屬房型已有未來庫存排程，若要刪除實體房間請先從未來庫存表移出。");
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

	// 使用Repository寫好的動態查詢
	public List<RoomVO> searchRooms(Integer roomId, Integer roomTypeId, Boolean roomSwitchStatus) {
		// 呼叫剛剛在 Repository 寫好的動態查詢
		List<RoomVO> list = repository.findRoomsByCriteria(roomId, roomTypeId, roomSwitchStatus);

		// 補上房型名稱（維持你原本 findAll / findById 的好習慣）
		for (RoomVO room : list) {
			RoomTypeVO type = roomTypeService.getOneRoomType(room.getRoomTypeId());
			if (type != null) {
				room.setRoomTypeName(type.getRoomTypeName());
			}
		}
		return list;

	}
}
