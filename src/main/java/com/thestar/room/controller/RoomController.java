package com.thestar.room.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

		List<Integer> existingRoomIds = roomService.findAll().stream().map(RoomVO::getRoomId)
				.collect(Collectors.toList());
		model.addAttribute("existingRoomIds", existingRoomIds);
		return "admin/room/roomForm";
	}

	// 執行新增
	@PostMapping("/insert")
	public String insert(RoomVO roomVO, RedirectAttributes redirectAttributes) {
		try {
			// 1. 容量限制檢查
			if (roomService.findAll().size() >= 30) {
				redirectAttributes.addFlashAttribute("errorMessage", "新增失敗：房間總數已達上限！");
				return "redirect:/room/manage";
			}

			// 2. 重複編號檢查
			if (roomService.existsById(roomVO.getRoomId())) {
				redirectAttributes.addFlashAttribute("errorMessage", "錯誤：房間編號 " + roomVO.getRoomId() + " 已存在！");
				return "redirect:/room/add";
			}

			// 3. 正常儲存
			roomService.save(roomVO);
			redirectAttributes.addFlashAttribute("successMessage", "房間新增成功！");
			return "redirect:/room/manage";

		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("errorMessage", "新增失敗，原因：" + e.getMessage());
	        return "redirect:/room/add";
		}
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
	public String update(RoomVO roomVO, RedirectAttributes redirectAttributes) {
		try {
			// 1. 直接將前端傳入的 roomVO 傳給 Service
			// Service 層會處理：
			// (a) 撈出原始資料比對
			// (b) 檢查「已入住不可下架」的業務規則
			// (c) 執行儲存
			roomService.save(roomVO);

			redirectAttributes.addFlashAttribute("successMessage", "房間編號 " + roomVO.getRoomId() + " 修改成功！");
		} catch (ResponseStatusException e) {
			// 2. 捕捉 Service 拋出的業務錯誤 (例如：已入住無法變更上架狀態)
			redirectAttributes.addFlashAttribute("errorMessage", e.getReason());
		} catch (Exception e) {
			// 3. 捕捉其他非預期系統錯誤
			redirectAttributes.addFlashAttribute("errorMessage", "修改失敗：系統發生錯誤，請稍後再試。");
		}

		// 一律轉回列表頁
		return "redirect:/room/manage";
	}

	// 執行刪除
	@PostMapping("/delete")
	public String delete(@RequestParam("roomId") Integer roomId, RedirectAttributes redirectAttributes) {
		try {
			// 呼叫 Service 執行刪除 (記得 Service 裡面要有我們剛剛討論的邏輯檢查)
			roomService.deleteById(roomId);
			redirectAttributes.addFlashAttribute("successMessage", "房間編號 " + roomId + " 已成功刪除！");

		} catch (ResponseStatusException e) {
			// 這是我們在 Service 拋出的自定義錯誤 (例如：已有住宿紀錄)
			redirectAttributes.addFlashAttribute("errorMessage", e.getReason());

		} catch (Exception e) {
			// 捕捉其他未預期的系統錯誤 (例如資料庫連線中斷)
			redirectAttributes.addFlashAttribute("errorMessage", "刪除失敗：該房間可能已有相關關聯資料，無法刪除。");
		}

		// 一律轉回列表頁
		return "redirect:/room/manage";
	}

}
