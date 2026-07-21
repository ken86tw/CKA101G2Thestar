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
	public String listAll(@RequestParam(value = "roomId", required = false) Integer roomId,
			@RequestParam(value = "roomTypeId", required = false) Integer roomTypeId,
			@RequestParam(value = "roomSwitchStatus", required = false) Boolean roomSwitchStatus, Model model) {

		// 1. 依據傳入的條件進行搜尋（用於畫面上表格的呈現）
		List<RoomVO> roomList = roomService.searchRooms(roomId, roomTypeId, roomSwitchStatus);

		model.addAttribute("roomList", roomList);
		model.addAttribute("roomTypeList", roomTypeService.getAllRoomTypes()); // 供下拉選單使用

		// 2. 💡【關鍵修正】取得「完全不受搜尋條件影響」的真實總房間數與可用額度
		List<RoomVO> allRooms = roomService.findAll(); // 抓出全部實體房間
		int totalRoomCount = allRooms.size();

		// 假設你的「剩餘可用額度」是指還可以新增幾間房間（例如上限 30 間扣掉現有房間總數）
		// 或者如果是指各房型庫存，這裡可以帶入計算邏輯
		int remainingQuota = 30 - totalRoomCount; // 依據你原本上限 30 間的邏輯

		model.addAttribute("totalRoomCount", totalRoomCount);
		model.addAttribute("remainingQuota", remainingQuota);

		model.addAttribute("paramRoomId", roomId);
		model.addAttribute("paramRoomTypeId", roomTypeId);
		model.addAttribute("paramRoomSwitchStatus", roomSwitchStatus);

		return "admin/room/roomManage";
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
	public String editInput(@PathVariable("roomId") Integer roomId,
			@RequestParam(value = "queryRoomId", required = false) Integer queryRoomId,
			@RequestParam(value = "queryRoomTypeId", required = false) Integer queryRoomTypeId,
			@RequestParam(value = "queryRoomSwitchStatus", required = false) Boolean queryRoomSwitchStatus,
			Model model) {

		// 1. 根據 ID 查詢現有的房間資料
		RoomVO roomVO = roomService.findById(roomId);
		model.addAttribute("roomVO", roomVO);

		// 2. 帶入房型列表
		model.addAttribute("roomTypeList", roomTypeService.getAllRoomTypes());

		// 3. 把搜尋條件再傳回修改頁面，以便送出表單時能帶回去
		model.addAttribute("queryRoomId", queryRoomId);
		model.addAttribute("queryRoomTypeId", queryRoomTypeId);
		model.addAttribute("queryRoomSwitchStatus", queryRoomSwitchStatus);

		return "admin/room/roomForm";
	}

	// 執行修改
	@PostMapping("/update")
	public String update(RoomVO roomVO, @RequestParam(value = "queryRoomId", required = false) Integer queryRoomId,
			@RequestParam(value = "queryRoomTypeId", required = false) Integer queryRoomTypeId,
			@RequestParam(value = "queryRoomSwitchStatus", required = false) Boolean queryRoomSwitchStatus,
			RedirectAttributes redirectAttributes) {
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
		return "redirect:/room/manage?roomId=" + (queryRoomId != null ? queryRoomId : "") + "&roomTypeId="
				+ (queryRoomTypeId != null ? queryRoomTypeId : "") + "&roomSwitchStatus="
				+ (queryRoomSwitchStatus != null ? queryRoomSwitchStatus : "");
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
