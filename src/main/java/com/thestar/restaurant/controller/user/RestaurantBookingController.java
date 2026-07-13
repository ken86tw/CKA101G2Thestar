package com.thestar.restaurant.controller.user;

import java.time.LocalDate; // 👈 1. 換成新版的 LocalDate
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.thestar.member.entity.MemberVO;
import com.thestar.restaurant.entity.AvailableTableVO;
import com.thestar.restaurant.entity.ReservationStatus;
import com.thestar.restaurant.entity.RestaurantReservationVO;
import com.thestar.restaurant.entity.RestaurantTableVO; 
import com.thestar.restaurant.service.AvailableTableService;
import com.thestar.restaurant.service.RestaurantReservationService;
import com.thestar.restaurant.service.RestaurantReviewService;

@Controller
@RequestMapping("/restaurant/booking")
public class RestaurantBookingController {

	@Autowired
	private RestaurantReservationService reservationService;

	@Autowired
	private AvailableTableService availableTableService;
	
	@Autowired
	private RestaurantReviewService reviewService;

	@GetMapping("/list")
	public String listMemberBookings(Model model) {
		// 1. 依需求暫時寫死目前登入會員 ID 為 1
		Integer currentMemberId = 1; 
		
		// 2. 呼叫 Service 取得該會員的所有訂位紀錄
		List<RestaurantReservationVO> myReservations = reservationService.getByMemberId(currentMemberId);
		
		// 3. 建立一個 Map 用來存放「訂位ID -> 評論物件」的對應關係
		java.util.Map<Integer, com.thestar.restaurant.entity.RestaurantReviewVO> reviewMap = new java.util.HashMap<>();
		
		for (RestaurantReservationVO res : myReservations) {
			// 透過你提供的 getByReservationId 方法撈取評論
			com.thestar.restaurant.entity.RestaurantReviewVO review = reviewService.getByReservationId(res.getReservationId());
			if (review != null) {
				reviewMap.put(res.getReservationId(), review);
			}
		}
		
		// 4. 將訂位列表與評論 Map 帶到前端
		model.addAttribute("myReservations", myReservations);
		model.addAttribute("reviewMap", reviewMap);
		
		// 5. 導向指定的網頁路徑
		return "user/restaurant/booking/list"; 
	}
	
	
	@GetMapping("/add")
	public String bookingPage(Model model) {
		availableTableService.initializeMonthlyTables();

		if (!model.containsAttribute("reservationVO")) {
			RestaurantReservationVO reservationVO = new RestaurantReservationVO();
			MemberVO member = new MemberVO();
			member.setMemberId(1); // 預設目前登入會員
			reservationVO.setMemberVO(member);

			int defaultGuests = 2;
			List<LocalDate> availableDates = availableTableService.getAvailableDatesByGuests(defaultGuests); // 👈 換成 LocalDate
			model.addAttribute("availableDates", availableDates);

			if (!availableDates.isEmpty()) {
				LocalDate firstDate = availableDates.get(0); // 👈 換成 LocalDate
				List<AvailableTableVO> availableSessions = availableTableService
						.getAvailableSessionsByDateAndGuests(firstDate, defaultGuests);
				model.addAttribute("availableSessions", availableSessions);
			}

			model.addAttribute("reservationVO", reservationVO);
		}
		return "user/restaurant/booking/add";
	}

	/**
	 * 2. 處理「人數改變」的局部刷新 AJAX 請求
	 */
	@GetMapping("/api/options")
	public String getBookingOptionsByGuests(@RequestParam("guests") int guests, Model model) {
		List<LocalDate> availableDates = availableTableService.getAvailableDatesByGuests(guests); // 👈 換成 LocalDate
		model.addAttribute("availableDates", availableDates);

		if (!availableDates.isEmpty()) {
			LocalDate firstDate = availableDates.get(0); // 👈 換成 LocalDate
			List<AvailableTableVO> availableSessions = availableTableService
					.getAvailableSessionsByDateAndGuests(firstDate, guests);
			model.addAttribute("availableSessions", availableSessions);
		}

		return "user/restaurant/booking :: bookingOptions";
	}

	/**
	 * 3. 處理表單送出
	 */
	@PostMapping("/submit")
	public String submitBooking(@ModelAttribute("reservationVO") RestaurantReservationVO reservationVO,
			BindingResult result, 
			@RequestParam("guests") int guests, 
			Model model, 
			RedirectAttributes redirectAttributes) {

		// 🛠️ 1. 人數桌型分流判定並綁定到 Entity
		RestaurantTableVO tableVO = new RestaurantTableVO();
		if (guests >= 1 && guests <= 4) {
			tableVO.setTableTypeId(2); 
		} else if (guests >= 5 && guests <= 10) {
			tableVO.setTableTypeId(1); 
		} else {
			model.addAttribute("errorMessage", "訂位人數不符合規範（限制 1-10 人）。");
			
			// 💡 如果 reservationVO.getDate() 回傳的是 java.sql.Date，轉成 LocalDate 再帶入
			LocalDate localDate = (reservationVO.getDate() != null) ? ((java.sql.Date) reservationVO.getDate()).toLocalDate() : null;
			prepareFormDropLists(model, guests, localDate);
			return "user/restaurant/booking";
		}
		reservationVO.setRestaurantTableVO(tableVO);

		// 🛠️ 2. 補足其餘必要的系統內定欄位
		if (reservationVO.getReviewStatus() == null) {
			reservationVO.setReviewStatus(false);
		}
		if (reservationVO.getReservationStatus() == null) {
			try {
				reservationVO.setReservationStatus(ReservationStatus.values()[0]);
			} catch (Exception e) {
				// 忽略
			}
		}

		// 🛠️ 3. 檢查欄位驗證錯誤
		if (result.hasErrors()) {
			model.addAttribute("errorMessage", "輸入資料有誤，請檢查欄位。");
			LocalDate localDate = (reservationVO.getDate() != null) ? ((java.sql.Date) reservationVO.getDate()).toLocalDate() : null;
			prepareFormDropLists(model, guests, localDate);
			return "user/restaurant/booking";
		}

		try {
			// 🛠️ 4. 執行寫入資料庫
			reservationService.addReservation(reservationVO);

			// 🛠️ 5. 寫入成功後，根據桌型扣減 AvailableTable 的庫存數量
			// 💡 這裡將選取的日期安全轉為 LocalDate，以配合新的 Service 參數
			LocalDate selectedDate = ((java.sql.Date) reservationVO.getDate()).toLocalDate();
			Integer selectedSessionId = reservationVO.getBusinessHoursVO().getSessionId();

			if (tableVO.getTableTypeId() == 2) {
				availableTableService.decreaseSmallTableCount(selectedDate, selectedSessionId);
			} else {
				availableTableService.decreaseLargeTableCount(selectedDate, selectedSessionId);
			}

			redirectAttributes.addFlashAttribute("successMessage", "恭喜您！訂位預約成功！");
			return "redirect:/restaurant/booking/list";

		} catch (Exception e) {
			model.addAttribute("errorMessage", "訂位失敗：" + e.getMessage());
			LocalDate localDate = (reservationVO.getDate() != null) ? ((java.sql.Date) reservationVO.getDate()).toLocalDate() : null;
			prepareFormDropLists(model, guests, localDate);
			return "user/restaurant/booking";
		}
	}

	/**
	 * 💡 抽取出來的私有方法：專門用來在流程失敗時重塞下拉選單資料
	 */
	private void prepareFormDropLists(Model model, int guests, LocalDate selectedDate) { // 👈 換成 LocalDate
		List<LocalDate> availableDates = availableTableService.getAvailableDatesByGuests(guests); // 👈 換成 LocalDate
		model.addAttribute("availableDates", availableDates);
		
		if (selectedDate != null) {
			model.addAttribute("availableSessions", 
				availableTableService.getAvailableSessionsByDateAndGuests(selectedDate, guests));
		} else if (!availableDates.isEmpty()) {
			model.addAttribute("availableSessions", 
				availableTableService.getAvailableSessionsByDateAndGuests(availableDates.get(0), guests));
		}
	}
}