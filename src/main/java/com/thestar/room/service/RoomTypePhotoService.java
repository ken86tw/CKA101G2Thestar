package com.thestar.room.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thestar.room.dto.RoomTypePhotoDTO;
import com.thestar.room.entity.RoomTypePhotoVO;
import com.thestar.room.entity.RoomTypeVO;
import com.thestar.room.repository.RoomTypePhotoRepository;
import com.thestar.room.repository.RoomTypeRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class RoomTypePhotoService {

	@Autowired
	private RoomTypePhotoRepository roomTypePhotoRepository;

	@Autowired
	private RoomTypeRepository roomTypeRepository;

	@PersistenceContext
	private EntityManager em; // 用於建立關聯的代理物件

	// 新增房型圖片
	@Transactional
	public RoomTypePhotoVO addRoomTypePhoto(RoomTypePhotoDTO request) {
		if (request.getRoomTypePic() == null || request.getRoomTypePic().isEmpty()) {
			throw new IllegalArgumentException("必須上傳一張圖片");
		}
		// 使用 getReference 取得 Proxy 物件，避免不必要的查詢
		RoomTypeVO roomType = em.getReference(RoomTypeVO.class, request.getRoomTypeId());

		try {
			RoomTypePhotoVO photo = new RoomTypePhotoVO();
			photo.setRoomTypeVO(roomType); // 正確設定關聯物件
			photo.setRoomTypePic(request.getRoomTypePic().getBytes());

			return roomTypePhotoRepository.save(photo);
		} catch (java.io.IOException e) {
			throw new RuntimeException("圖片處理失敗", e);
		}
	}

	// 刪除單一房型圖片
	@Transactional
	public void deletePhotoById(Integer photoId) {
		if (!roomTypePhotoRepository.existsById(photoId)) {
			throw new IllegalArgumentException("找不到該圖片 ID: " + photoId);
		}
		roomTypePhotoRepository.deleteById(photoId);
	}

	// 查詢單一圖片
	public RoomTypePhotoVO getPhotoById(Integer photoId) {
		return roomTypePhotoRepository.findById(photoId)
				.orElseThrow(() -> new IllegalArgumentException("找不到該圖片 ID: " + photoId));
	}

	// 更新圖片
	@Transactional
	public void updateRoomTypePhoto(RoomTypePhotoDTO request) {
		// 1. 直接根據圖片的唯一 ID 取得物件
		RoomTypePhotoVO photo = roomTypePhotoRepository.findById(request.getRoomTypePhotoId())
				.orElseThrow(() -> new IllegalArgumentException("找不到該圖片記錄"));
		try {
			// 2. 只有在使用者真的有上傳新圖時，才更新 byte[]
			if (request.getRoomTypePic() != null && !request.getRoomTypePic().isEmpty()) {
				photo.setRoomTypePic(request.getRoomTypePic().getBytes());
			}

			// 3. 房型 ID 若有變更（通常不會），可在這裡處理
			// 若房型關聯是固定的，這裡其實不需要重複 set
			roomTypePhotoRepository.save(photo);
		} catch (java.io.IOException e) {
			throw new RuntimeException("圖片處理失敗", e);
		}

		try {
			if (request.getRoomTypePic() != null && !request.getRoomTypePic().isEmpty()) {
				photo.setRoomTypePic(request.getRoomTypePic().getBytes());
				roomTypePhotoRepository.save(photo);
			}
		} catch (java.io.IOException e) {
			throw new RuntimeException("圖片處理失敗", e);
		}
	}

	// 根據房型ID查詢圖片
	public List<RoomTypePhotoVO> getPhotosByRoomTypeId(Integer roomTypeId) {
		return roomTypePhotoRepository.findByRoomTypeVORoomTypeId(roomTypeId);
	}

	// 查詢所有房型圖片
	public List<RoomTypePhotoVO> getAllPhotos() {
		return roomTypePhotoRepository.findAll();
	}
}
