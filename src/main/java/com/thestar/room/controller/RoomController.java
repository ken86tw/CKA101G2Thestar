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
		RoomTypeVO roomTypeVO = roomTypeService.getOneRoomType(typeId);
//		roomVO.setRoomTypeVO(typeVO);

		roomVO.setRoomTypeId(typeId);
		roomService.save(roomVO);
		return "redirect:/room/manage";
	}

	// 進入修改頁面 (回填資料)
    @GetMapping("/edit/{roomId}")
    public String editInput(@PathVariable("roomId") Integer roomId, Model model) {
        // 1. 根據 ID 查詢現有的房間資料
        RoomVO roomVO = roomService.findById(roomId);
        model.addAttribute("roomVO", roomVO);
        
        // 2. 帶入房型列表，讓頁面上的下拉選單可以選擇
        model.addAttribute("roomTypeList", roomTypeService.getAllRoomTypes());
        
        // 3. 導向到同一個表單頁面 (roomForm.html)
        return "admin/room/roomForm";
    }

    // 執行修改
    @PostMapping("/update")
    public String update(RoomVO roomVO, @RequestParam("roomTypeId") Integer typeId) {
        // 1. 設定關聯的房型 ID
        roomVO.setRoomTypeId(typeId);
        
        // 2. 呼叫 Service 的更新方法
        // 注意：這裡假設你的 service 有 update 方法，若沒有，通常也是呼叫 save
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
