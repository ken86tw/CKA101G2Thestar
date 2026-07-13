package com.thestar.room.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thestar.room.entity.RoomTypeVO;
import com.thestar.room.entity.RoomVO;
import com.thestar.room.service.RoomService;
import com.thestar.room.service.RoomTypeService;

@Controller
@RequestMapping("/room")
public class RoomController {

	@Autowired
	private RoomService roomService;

	@Autowired
	private RoomTypeService roomTypeService;

	// 列表頁面
	@GetMapping("/manage")
	public String listAll(Model model) {
		model.addAttribute("roomList", roomService.findAll());
		return "admin/room/roomManage"; // 對應你的 HTML 路徑
	}

	// 檢視房間詳細資料
	@GetMapping("/details/{roomId}")
	public String getOneRoom(@PathVariable("roomId") Integer roomId, Model model) {
		model.addAttribute("roomVO", roomService.findById(roomId));
		return "admin/room/roomDetails"; // 導向顯示詳情的頁面
	}

	// 進入新增頁面
	@GetMapping("/add")
	public String addInput(Model model) {
		model.addAttribute("roomVO", new RoomVO());
		model.addAttribute("roomTypeList", roomTypeService.getAllRoomTypes());
		return "admin/room/roomForm";
	}

	// 執行新增
	@PostMapping("/insert")
	public String insert(RoomVO roomVO, @RequestParam("roomTypeId") Integer typeId) {
		// 手動將 ID 轉成物件
		RoomTypeVO typeVO = roomTypeService.getOneRoomType(typeId);
		roomVO.setRoomTypeVO(typeVO);

		roomService.save(roomVO);
		return "redirect:/room/manage";
	}

	// 進入修改頁面
	@GetMapping("/edit/{roomId}")
	public String editInput(@PathVariable("roomId") Integer roomId, Model model) {
		model.addAttribute("roomVO", roomService.findById(roomId));
		model.addAttribute("roomTypeList", roomTypeService.getAllRoomTypes());
		return "admin/room/roomForm";
	}

	// 執行修改
	@PostMapping("/save") // 將 insert 和 update 合併
	public String save(RoomVO roomVO) {
		// 1. 從前端傳來的 roomVO 中取出 roomTypeVO 的 ID
		if (roomVO.getRoomTypeVO() != null && roomVO.getRoomTypeVO().getRoomTypeId() != null) {
			RoomTypeVO typeVO = roomTypeService.getOneRoomType(roomVO.getRoomTypeVO().getRoomTypeId());
			// 2. 將完整的物件塞回 roomVO
			roomVO.setRoomTypeVO(typeVO);
		}
		roomService.save(roomVO);
		return "redirect:/room/manage";
	}

	// 執行刪除
	@PostMapping("/delete")
	public String delete(@RequestParam("roomId") Integer roomId) {
		roomService.deleteById(roomId);
		return "redirect:/room/manage";
	}

}
