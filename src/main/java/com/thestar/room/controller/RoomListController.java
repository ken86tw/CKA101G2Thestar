package com.thestar.room.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.thestar.room.entity.RoomTypePhotoVO;
import com.thestar.room.entity.RoomTypeVO;
import com.thestar.room.service.RoomService;
import com.thestar.room.service.RoomTypePhotoService;
import com.thestar.room.service.RoomTypeService;

@Controller
@RequestMapping("/roomList") // 統一網址前綴
public class RoomListController {

	@Autowired
	private RoomService roomService;

	@Autowired
	private RoomTypeService roomTypeService;

	@Autowired
	private RoomTypePhotoService photoService;

	// 負責顯示列表頁 (roomList.html)
	@GetMapping("/list")
	public String showList(Model model) {
		List<RoomTypeVO> list = roomTypeService.getAllRoomTypes();

		if (list != null) {
			for (RoomTypeVO roomType : list) {
				RoomTypePhotoVO photo = photoService.findFirstByRoomTypeId(roomType.getRoomTypeId());
				roomType.setFirstPhoto(photo);
			}
		}
		model.addAttribute("activeRoomList", list);
		return "user/room/roomList"; // 回傳列表頁的視圖
	}

	// 2. 負責顯示詳細頁：需要 @PathVariable，路徑為 /roomList/detail/{id}
	@GetMapping("/detail/{id}")
	public String showDetail(@PathVariable("id") Integer id, Model model) {
		RoomTypeVO roomType = roomTypeService.getOneRoomType(id);

		if (roomType == null) {
			return "redirect:/roomList/list";
		}

		// 取得該房型所有照片
		List<RoomTypePhotoVO> photos = photoService.getPhotosByRoomTypeId(id);

		// 【關鍵修正】：如果照片列表不為空，將第一張照片設定給 roomType 的 firstPhoto 屬性
		if (photos != null && !photos.isEmpty()) {
			roomType.setFirstPhoto(photos.get(0));
		} else {
			roomType.setFirstPhoto(null); // 明確確保沒有照片時為 null
		}

		model.addAttribute("room", roomType);
		model.addAttribute("photoList", photos);

		return "user/room/roomListDetails";
	}

}
