package com.thestar.room.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

@Controller
@RequestMapping("/roomtypephoto")
public class RoomTypePhotoController {

	@Autowired
	private RoomTypePhotoService service;

	@Autowired
	private RoomTypeService roomTypeService;

	@Autowired
	private ResourceLoader resourceLoader;

	@GetMapping("/manage")
	public String listAllPhotos(Model model) {
	    // 只需要傳入所有的房型清單，前端會用這些 ID 去抓第一張圖
	    List<RoomTypeVO> rooms = roomTypeService.getAllRoomTypes();
	    
	    model.addAttribute("roomTypeList", rooms);
	    return "admin/room/photoManage";
	}

	// 顯示新增圖片的頁面
	@GetMapping("/addPage")
	public String showAddPage(@RequestParam(required = false) Integer roomTypeId, Model model) {
		RoomTypePhotoDTO dto = new RoomTypePhotoDTO();
		// 如果從總覽頁傳入了 roomTypeId，就自動填入，達到「指定房型」的效果
		if (roomTypeId != null) {
			dto.setRoomTypeId(roomTypeId);
		
		model.addAttribute("photoList", service.getPhotosByRoomTypeId(roomTypeId));
    }
		model.addAttribute("photoVO", dto);
		model.addAttribute("roomTypeList", roomTypeService.getAllRoomTypes());
		return "admin/room/photoForm";
	}

	// 更新圖片頁面 (進入修改頁)
	@GetMapping("/edit/{photoId}")
	public String showEditPage(@PathVariable Integer photoId, Model model) {
		// 1. 查詢資料
		RoomTypePhotoVO vo = service.getPhotoById(photoId);

		// 2. 防呆：如果該 ID 的圖片不存在，導回列表頁或進行錯誤處理
		if (vo == null) {
			return "redirect:/roomtypephoto/manage";
		}

		// 3. 將 VO 轉為 DTO 傳給前端
		RoomTypePhotoDTO dto = new RoomTypePhotoDTO();
		dto.setRoomTypePhotoId(vo.getRoomTypePhotoId());

		// 這裡確保即使關聯資料為 null 也不會噴出 NullPointerException
		if (vo.getRoomTypeVO() != null) {
			dto.setRoomTypeId(vo.getRoomTypeVO().getRoomTypeId());
		}

		// 4. 將 DTO 傳給前端 (現在 photoVO 就是 DTO)
		model.addAttribute("photoVO", dto);

		// 5. 傳遞下拉選單列表
		model.addAttribute("roomTypeList", roomTypeService.getAllRoomTypes());

		return "admin/room/photoForm";
	}

	// 儲存
	@PostMapping("/save")
	public String savePhoto(@RequestParam(value = "file", required = false) MultipartFile file,
			RoomTypePhotoDTO request, Model model) {
		try {
			// 將檔案存入 DTO，確保 Service 層能讀取到
			if (file != null && !file.isEmpty()) {
				request.setRoomTypePic(file);
			}

			if (request.getRoomTypePhotoId() == null) {
				service.addRoomTypePhoto(request);
			} else {
				service.updateRoomTypePhoto(request);
			}
			return "redirect:/roomtypephoto/manage";
		} catch (Exception e) {
			model.addAttribute("roomTypeList", roomTypeService.getAllRoomTypes());
			// 失敗時直接回傳 DTO，與 HTML 的 th:object="${photoVO}" 型別一致
			model.addAttribute("photoVO", request);
			model.addAttribute("errorMessage", "儲存失敗: " + e.getMessage());
			return "admin/room/photoForm";
		}
	}

	@GetMapping("/display/{roomTypeId}")
	public ResponseEntity<byte[]> displayPhoto(@PathVariable Integer roomTypeId) {
		List<RoomTypePhotoVO> photos = service.getPhotosByRoomTypeId(roomTypeId);
		// 檢查是否有照片，且照片本身的 byte[] 不為 null
		if (photos != null && !photos.isEmpty() && photos.get(0).getRoomTypePic() != null) {
			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(photos.get(0).getRoomTypePic());
		}
		// --- 沒圖時，回傳預設圖片 ---
		try {
			Resource resource = resourceLoader.getResource("classpath:static/images/noimage.jpg");
			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource.getInputStream().readAllBytes());
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}

	// 1. 給總覽頁用：顯示該房型的第一張圖
	@GetMapping("/display/room/{roomTypeId}")
	public ResponseEntity<byte[]> displayRoomTypePhoto(@PathVariable Integer roomTypeId) {
		List<RoomTypePhotoVO> photos = service.getPhotosByRoomTypeId(roomTypeId);
		if (photos != null && !photos.isEmpty() && photos.get(0).getRoomTypePic() != null) {
			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(photos.get(0).getRoomTypePic());
		}
		return getNoImage(); // 抽出共用方法
	}

	// 2. 給新增/修改頁面用：顯示特定的那張圖 (根據 photoId)
	@GetMapping("/display/photo/{photoId}")
	public ResponseEntity<byte[]> displaySinglePhoto(@PathVariable Integer photoId) {
		RoomTypePhotoVO photo = service.getPhotoById(photoId);
		if (photo != null && photo.getRoomTypePic() != null) {
			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(photo.getRoomTypePic());
		}
		return getNoImage();
	}

	// 共用方法：處理沒圖的情況
	private ResponseEntity<byte[]> getNoImage() {
		try {
			Resource resource = resourceLoader.getResource("classpath:static/images/noimage.jpg");
			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource.getInputStream().readAllBytes());
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}

	// 刪除圖片
	@PostMapping("/delete/{photoId}")
	public String deletePhoto(@PathVariable Integer photoId) {
		service.deletePhotoById(photoId); // 你需要確認 Service 有此方法
		return "redirect:/room/manage";
	}

}
