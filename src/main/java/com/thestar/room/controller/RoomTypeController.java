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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.thestar.room.dto.RoomTypePhotoDTO;
import com.thestar.room.entity.RoomTypePhotoVO;
import com.thestar.room.entity.RoomTypeVO;
import com.thestar.room.service.RoomService;
import com.thestar.room.service.RoomTypePhotoService;
import com.thestar.room.service.RoomTypeService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/roomtype") // 設定網址路徑
public class RoomTypeController {

	@Autowired
	private RoomService roomService;

	@Autowired
	private RoomTypeService service;

	@Autowired
	private RoomTypePhotoService photoService;

	// 所有房型總覽
	@GetMapping("/manage")
	public String showRoomTypesPage(Model model) {
		List<RoomTypeVO> list = service.getAllRoomTypes();
		java.util.Map<Integer, Integer> roomCountsMap = new java.util.HashMap<>();
		for (RoomTypeVO type : list) {
			roomCountsMap.put(type.getRoomTypeId(), (int) roomService.countRoomsByTypeId(type.getRoomTypeId()));
		}

		int actualTotalCount = roomService.findAll().size();
		int remaining = 30 - actualTotalCount;

		model.addAttribute("rooms", list);
		model.addAttribute("roomCountsMap", roomCountsMap);
		model.addAttribute("totalCount", list.size());
		model.addAttribute("totalAmount", actualTotalCount);
		model.addAttribute("remaining", remaining);

		return "admin/room/roomTypeManage";
	}

	// 檢視房型詳細資料
	@GetMapping("/details/{id}")
	public String getRoomTypeDetails(@PathVariable("id") Integer id, Model model) {
		// 1. 取得房型基本資料
		RoomTypeVO roomTypeVO = service.getOneRoomType(id);
		model.addAttribute("roomTypeVO", roomTypeVO);

		// 2. 【關鍵修正】取得該房型所有的照片列表，並傳入 model
		// 這樣 HTML 頁面才能透過 th:each="photo : ${photoList}" 讀到資料
		List<RoomTypePhotoVO> photos = photoService.getPhotosByRoomTypeId(id);
		model.addAttribute("photoList", photos);

		return "admin/room/roomTypeDetails"; // 對應到你的 HTML 檔名
	}

	// 進入新增頁面
	@GetMapping("/add")
	public String addRoomTypePage(Model model) {
		// 1. 建立一個空的房型物件
		model.addAttribute("roomTypeVO", new RoomTypeVO());

		// 2. [補充] 明確傳入 0，因為新增房型時庫存絕對是 0
		// 這樣 HTML 的顯示邏輯會更穩定
		model.addAttribute("minAllowedAmount", 0);

		return "admin/room/roomTypeForm";
	}

	// 執行新增
	@PostMapping("/insert")
	public String insert(@Valid RoomTypeVO roomTypeVO, BindingResult result,
			@RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles, Model model,
			RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			// 【重要】確保物件還在 Model 裡，否則欄位值會消失，錯誤也不會顯示
			model.addAttribute("roomTypeVO", roomTypeVO);
			model.addAttribute("minAllowedAmount", 0);
			System.out.println("發生驗證錯誤: " + result.getAllErrors());
			return "admin/room/roomTypeForm";
		}

		try {
			service.addRoomType(roomTypeVO);

			// 2. 圖片處理邏輯
			if (imageFiles != null) {
				for (MultipartFile file : imageFiles) {
					if (file != null && !file.isEmpty()) {
						RoomTypePhotoDTO photoDTO = new RoomTypePhotoDTO();
						photoDTO.setRoomTypeId(roomTypeVO.getRoomTypeId());
						photoDTO.setRoomTypePic(file);
						photoService.addRoomTypePhoto(photoDTO);
					}
				}
			}

			redirectAttributes.addFlashAttribute("successMessage", "房型新增成功！");
		} catch (Exception e) {
			model.addAttribute("errorMessage", "新增失敗：" + e.getMessage());
			model.addAttribute("minAllowedAmount", 0);
			return "admin/room/roomTypeForm";
		}

		return "redirect:/roomtype/manage";
	}

	// 進入修改頁面 (帶入特定 ID)
	@GetMapping("/edit/{id}")
	public String editRoomTypePage(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			// 1. 取得房型資料
			RoomTypeVO roomTypeVO = service.getOneRoomType(id);

			// 2. [新增] 檢查是否滿足修改條件 (重複使用 Service 的邏輯)
			// 這樣做的好處是使用者一點擊編輯，若不符合規則，直接被擋住，不會讓他進到表單畫面
			// 注意：這裡若拋出異常，會進到下方的 catch
			// 您可以另外在 Service 寫一個 public boolean canUpdate(Integer id) 來判斷

			model.addAttribute("roomTypeVO", roomTypeVO);
			model.addAttribute("photoList", photoService.getPhotosByRoomTypeId(id));
			model.addAttribute("minAllowedAmount", (int) roomService.countRoomsByTypeId(id));

			return "admin/room/roomTypeForm";

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "無法進入編輯頁面：" + e.getMessage());
			return "redirect:/roomtype/manage";
		}
	}

	// 執行修改
	@PostMapping("/update")
	public String update(@Valid RoomTypeVO roomTypeVO, BindingResult result,
			@RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles, Model model,
			RedirectAttributes redirectAttributes) {

		// 1. 錯誤處理 (如果有 @Valid 驗證錯誤)
		if (result.hasErrors()) {
			model.addAttribute("roomTypeVO", roomTypeVO);
			model.addAttribute("photoList", photoService.getPhotosByRoomTypeId(roomTypeVO.getRoomTypeId()));
			model.addAttribute("minAllowedAmount", (int) roomService.countRoomsByTypeId(roomTypeVO.getRoomTypeId()));
			return "admin/room/roomTypeForm";
		}

		// 2. [新增邏輯] 取得資料庫中原本的資料進行比對
		RoomTypeVO exist = service.getOneRoomType(roomTypeVO.getRoomTypeId());

		// 比對欄位 (名稱, 說明, 價格, 狀態)
		boolean isNameSame = exist.getRoomTypeName().equals(roomTypeVO.getRoomTypeName());
		boolean isContentSame = (exist.getRoomTypeContent() == null ? "" : exist.getRoomTypeContent())
				.equals(roomTypeVO.getRoomTypeContent() == null ? "" : roomTypeVO.getRoomTypeContent());
		boolean isPriceSame = exist.getRoomTypePrice().equals(roomTypeVO.getRoomTypePrice());
		boolean isStatusSame = exist.getRoomTypeStatus().equals(roomTypeVO.getRoomTypeStatus());
		boolean isCapacitySame = exist.getCapacity().equals(roomTypeVO.getCapacity());
		boolean isAmenitiesSame = (exist.getAmenities() == null ? "" : exist.getAmenities())
				.equals(roomTypeVO.getAmenities() == null ? "" : roomTypeVO.getAmenities());

		// 檢查是否有新圖片上傳
		boolean hasNewImages = (imageFiles != null && java.util.Arrays.stream(imageFiles).anyMatch(f -> !f.isEmpty()));

		// 如果所有欄位都一樣，且沒有上傳新圖片，則視為「未修改」
		if (isNameSame && isContentSame && isPriceSame && isStatusSame && isCapacitySame && isAmenitiesSame
				&& !hasNewImages) {
			redirectAttributes.addFlashAttribute("errorMessage", "資料未變更！請修改後再儲存！");
			return "redirect:/roomtype/manage";
		}

		// 3. 有變動，執行更新
		try {
			service.updateRoomType(roomTypeVO);

			// 處理新圖片
			if (hasNewImages) {
				for (MultipartFile file : imageFiles) {
					if (file != null && !file.isEmpty()) {
						RoomTypePhotoDTO photoDTO = new RoomTypePhotoDTO();
						photoDTO.setRoomTypeId(roomTypeVO.getRoomTypeId());
						photoDTO.setRoomTypePic(file);
						photoService.addRoomTypePhoto(photoDTO);
					}
				}
			}
			redirectAttributes.addFlashAttribute("successMessage", "房型更新成功！");
			return "redirect:/roomtype/manage";
		} catch (ResponseStatusException e) {
			// 確保只傳遞錯誤訊息，不要讓 Spring Boot 認為這是個未處理的例外
			redirectAttributes.addFlashAttribute("errorMessage", e.getReason());

			// 導向編輯頁面，頁面會重新讀取並顯示該訊息
			return "redirect:/roomtype/edit/" + roomTypeVO.getRoomTypeId();
		}

	}

	// 執行刪除指定房型
	@PostMapping("/delete/{id}")
	public String delete(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
		try {
			service.deleteRoomType(id);
			redirectAttributes.addFlashAttribute("successMessage", "房型刪除成功！");
		} catch (ResponseStatusException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getReason());
		}
		return "redirect:/roomtype/manage";
	}
}
