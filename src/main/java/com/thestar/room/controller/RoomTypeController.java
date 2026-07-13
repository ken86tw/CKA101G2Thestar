package com.thestar.room.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.thestar.room.dto.RoomTypePhotoDTO;
import com.thestar.room.entity.RoomTypePhotoVO;
import com.thestar.room.entity.RoomTypeVO;
import com.thestar.room.service.RoomTypePhotoService;
import com.thestar.room.service.RoomTypeService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/roomtype") // 設定網址路徑
public class RoomTypeController {

	@Autowired
	private RoomTypeService service;

	@Autowired
	private RoomTypePhotoService photoService;

	@GetMapping("/manage")
	public String showRoomTypesPage(Model model) {
		// 取得所有房型資料
		List<RoomTypeVO> list = service.getAllRoomTypes();
		// 計算總房型類別數
		int totalCount = list.size();
		// 計算所有房型的總庫存數量 (將每個房型的 roomTypeAmount 相加)
		int totalAmount = list.stream()
				.mapToInt(room -> room.getRoomTypeAmount() != null ? room.getRoomTypeAmount() : 0).sum();
		// 將資料放入 Model 中
		model.addAttribute("rooms", list);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("totalAmount", totalAmount);
		return "admin/room/roomTypeManage";
	}

	// 進入新增頁面
	@GetMapping("/add")
	public String addRoomTypePage(Model model) {
		model.addAttribute("roomTypeVO", new RoomTypeVO());
		return "admin/room/roomTypeForm";
	}

	// 執行新增
	@PostMapping("/insert")
	public String insert(@Valid RoomTypeVO roomTypeVO, BindingResult result,
			@RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
		if (result.hasErrors()) {
			return "admin/room/roomTypeForm";
		}

		// 1. 先新增房型以取得 roomTypeId
		service.addRoomType(roomTypeVO);

		// 2. 如果有圖片，透過 photoService 儲存
		if (imageFile != null && !imageFile.isEmpty()) {
			RoomTypePhotoDTO photoDTO = new RoomTypePhotoDTO();
			photoDTO.setRoomTypeId(roomTypeVO.getRoomTypeId());
			photoDTO.setRoomTypePic(imageFile); // 這裡請確保 DTO 支援 setRoomTypePic(MultipartFile)
			photoService.addRoomTypePhoto(photoDTO);
		}

		return "redirect:/roomtype/manage";
	}

	// 進入修改頁面 (帶入特定 ID)
	@GetMapping("/edit/{id}")
	public String editRoomTypePage(@PathVariable("id") Integer id, Model model) {
		RoomTypeVO roomTypeVO = service.getOneRoomType(id);
		model.addAttribute("roomTypeVO", roomTypeVO);
		return "admin/room/roomTypeForm";
	}

	// 執行修改
	@PostMapping("/update")
	public String update(@Valid RoomTypeVO roomTypeVO, BindingResult result,
			@RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
		if (result.hasErrors()) {
			return "admin/room/roomTypeForm";
		}

		// 1. 關鍵修改：先從資料庫撈出「原本」的物件，確保它是受管理的實體 (Managed Entity)
		RoomTypeVO existingRoom = service.getOneRoomType(roomTypeVO.getRoomTypeId());

		if (existingRoom == null) {
			// 防止 ID 不存在的情況
			return "redirect:/roomtype/manage";
		}

		// 2. 將前端傳來的資料「手動」覆寫到資料庫撈出來的物件上
		existingRoom.setRoomTypeName(roomTypeVO.getRoomTypeName());
		existingRoom.setRoomTypeAmount(roomTypeVO.getRoomTypeAmount());
		existingRoom.setRoomTypePrice(roomTypeVO.getRoomTypePrice());
		existingRoom.setRoomTypeStatus(roomTypeVO.getRoomTypeStatus());
		existingRoom.setRoomTypeContent(roomTypeVO.getRoomTypeContent());

		// 3. 儲存這個已經被修改過的「舊物件」
		service.updateRoomType(existingRoom);

		// 4. 圖片處理 (邏輯正確)
		if (imageFile != null && !imageFile.isEmpty()) {
			RoomTypePhotoDTO photoDTO = new RoomTypePhotoDTO();
			photoDTO.setRoomTypeId(existingRoom.getRoomTypeId()); // 
			photoDTO.setRoomTypePic(imageFile); // 和圖片
			List<RoomTypePhotoVO> photos = photoService.getPhotosByRoomTypeId(existingRoom.getRoomTypeId());
			if (photos.isEmpty()) {
				photoService.addRoomTypePhoto(photoDTO); // 
			} else {
				photoDTO.setRoomTypePhotoId(photos.get(0).getRoomTypePhotoId()); 
				photoService.updateRoomTypePhoto(photoDTO);
			}
		}

		return "redirect:/roomtype/manage";
	}

	// 執行刪除
	@PostMapping("/delete/{id}")
	public String delete(@PathVariable("id") Integer id) {
		service.deleteRoomType(id);
		return "redirect:/roomtype/manage";
	}
}
