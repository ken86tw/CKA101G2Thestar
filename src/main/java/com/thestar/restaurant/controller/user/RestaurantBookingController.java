package com.thestar.restaurant.controller.user;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.thestar.member.entity.MemberVO;
import com.thestar.member.service.MemberAuthService;
import com.thestar.member.service.MemberNotifyService;
import com.thestar.restaurant.dto.SessionStatusDTO;
import com.thestar.restaurant.entity.AvailableTableVO;
import com.thestar.restaurant.entity.BusinessHoursVO; // 👈 修正 2：補上 BusinessHoursVO 的匯入
import com.thestar.restaurant.entity.ReservationStatus;
import com.thestar.restaurant.entity.RestaurantReservationVO;
import com.thestar.restaurant.entity.RestaurantReviewVO;
import com.thestar.restaurant.entity.RestaurantTableVO;
import com.thestar.restaurant.service.AvailableTableService;
import com.thestar.restaurant.service.BusinessHoursService;
import com.thestar.restaurant.service.RestaurantReservationService;
import com.thestar.restaurant.service.RestaurantReviewService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/restaurant/booking")
public class RestaurantBookingController {

	@Autowired
	private RestaurantReservationService reservationService;

	@Autowired
	private AvailableTableService availableTableService;

	@Autowired
	private RestaurantReviewService reviewService;

	@Autowired
	private BusinessHoursService businessHoursService;

	@Autowired
	private MemberAuthService memberAuthService;

	@Autowired
	private MemberNotifyService memberNotifyService;

	@GetMapping("/list")
	public String listMemberBookings(Model model, HttpSession session) {
		MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
		if (loginMember == null) {
			return "redirect:/login.html" + "?redirect=/restaurant/booking/list";
		}
		Integer currentMemberId = loginMember.getMemberId();

		List<RestaurantReservationVO> myReservations = reservationService.getByMemberId(currentMemberId);

		Map<Integer, RestaurantReviewVO> reviewMap = new HashMap<>();

		for (RestaurantReservationVO res : myReservations) {
			RestaurantReviewVO review = reviewService
					.getByReservationId(res.getReservationId());
			if (review != null) {
				reviewMap.put(res.getReservationId(), review);
			}
		}

		model.addAttribute("myReservations", myReservations);
		model.addAttribute("reviewMap", reviewMap);
		model.addAttribute("loginMember", loginMember);

		return "user/restaurant/booking/list";
	}

	@GetMapping("/add")
	public String bookingPage(Model model, HttpSession session) {
		availableTableService.initializeMonthlyTables();

		MemberVO loginMember = getCurrentMember(session);
		if (loginMember == null) {
			return "redirect:/login.html" + "?redirect=/restaurant/booking/add";
		}
		model.addAttribute("loginMember", loginMember);

		if (!model.containsAttribute("reservationVO")) {
			RestaurantReservationVO reservationVO = new RestaurantReservationVO();
			reservationVO.setMemberVO(loginMember);

			LocalDate today = LocalDate.now();
			List<LocalDate> availableDates = today.datesUntil(today.plusMonths(1)).collect(Collectors.toList());

			model.addAttribute("availableDates", availableDates);
			model.addAttribute("reservationVO", reservationVO);
		}
		return "user/restaurant/booking/add";
	}

	// 👈 修正 1：清除本區塊所有看不見的隱形空白字元，現在 @RequestParam 能夠被正常解析了
	@GetMapping("/api/session-status")
	@ResponseBody
	public List<SessionStatusDTO> getSessionStatus(@RequestParam("guests") int guests,
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

		List<BusinessHoursVO> allSessions = businessHoursService.getAll();

		List<AvailableTableVO> availableTables = availableTableService.getAvailableSessionsByDateAndGuests(date,
				guests);

		java.util.Set<Integer> availableSessionIds = availableTables.stream().map(table -> table.getId().getSessionId())
				.collect(Collectors.toSet());

		return allSessions.stream().map(session -> {
			boolean isAvailable = availableSessionIds.contains(session.getSessionId());
			return new SessionStatusDTO(session.getSessionId(), session.getStartTime(), session.getEndTime(),
					isAvailable);
		}).collect(Collectors.toList());
	}

	@PostMapping("/submit")
	public String submitBooking(@ModelAttribute("reservationVO") RestaurantReservationVO reservationVO,
			BindingResult result, @RequestParam("guests") int guests, Model model,
			RedirectAttributes redirectAttributes, HttpSession session) {

		MemberVO loginMember = getCurrentMember(session);
		if (loginMember == null) {
			return "redirect:/login.html" + "?redirect=/restaurant/booking/add";
		}
		reservationVO.setMemberVO(loginMember);

		RestaurantTableVO tableVO = new RestaurantTableVO();
		if (guests >= 1 && guests <= 4) {
			tableVO.setTableTypeId(2);
		} else if (guests >= 5 && guests <= 10) {
			tableVO.setTableTypeId(1);
		} else {
			// 修正 1：補上 loginMember
			model.addAttribute("loginMember", loginMember);
			model.addAttribute("errorMessage", "訂位人數不符合規範（限制 1-10 人）。");

			LocalDate localDate = (reservationVO.getDate() != null)
					? ((java.sql.Date) reservationVO.getDate()).toLocalDate()
					: null;
			prepareFormDropLists(model, guests, localDate);
			return "user/restaurant/booking/add"; // 💡 提醒：確保這裡路徑跟 GET 一致是 add
		}
		reservationVO.setRestaurantTableVO(tableVO);

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

		if (result.hasErrors()) {
			// 修正 2：補上 loginMember
			model.addAttribute("loginMember", loginMember);
			model.addAttribute("errorMessage", "輸入資料有誤，請檢查欄位。");
			LocalDate localDate = (reservationVO.getDate() != null)
					? ((java.sql.Date) reservationVO.getDate()).toLocalDate()
					: null;
			prepareFormDropLists(model, guests, localDate);
			return "user/restaurant/booking/add";
		}

		try {
			LocalDate selectedDate = ((java.sql.Date) reservationVO.getDate()).toLocalDate();
			Integer selectedSessionId = reservationVO.getBusinessHoursVO().getSessionId();

			// 💡 步驟 A：先扣減桌數（如果沒位子，這裡會直接噴 Exception 跳到 catch）
			if (tableVO.getTableTypeId() == 2) {
				availableTableService.decreaseSmallTableCount(selectedDate, selectedSessionId);
			} else {
				availableTableService.decreaseLargeTableCount(selectedDate, selectedSessionId);
			}

			// 💡 步驟 B：扣桌成功，代表搶到位子了！這時候才真正寫入預約單
			reservationService.addReservation(reservationVO);
			
			String timeRange = "未知時段";
		    try {
		        BusinessHoursVO bh = businessHoursService.getOneBusinessHours(selectedSessionId);
		        if (bh != null && bh.getStartTime() != null && bh.getEndTime() != null) {
		            // 組合格式如：12:00 ~ 14:00
		            timeRange = bh.getStartTime() + " ~ " + bh.getEndTime();
		        }
		    } catch (Exception e) {
		        // 防呆：若查無此時段，則沿用原來的 ID 當作保底顯示
		        timeRange = "時段編號 " + selectedSessionId;
		    }
			
			String notificationContent = "【The Star 餐廳】您的訂位預約成功！日期：" + selectedDate + "，時段：" + timeRange
					+ "訂位人數:" + guests;

			memberNotifyService.createNotification(loginMember.getMemberId(), notificationContent); // 👈 這裡記得補上右括號與分號

			redirectAttributes.addFlashAttribute("successMessage", "恭喜您！訂位預約成功！");
			return "redirect:/restaurant/booking/list";

		} catch (Exception e) {
			// 這裡可以完美捕捉「非常抱歉，該時段的...已被訂滿！」的訊息，再也不會噴出無情的 500 錯誤頁面了
			model.addAttribute("loginMember", loginMember);
			model.addAttribute("errorMessage", "訂位失敗：" + e.getMessage());

			LocalDate localDate = (reservationVO.getDate() != null)
					? ((java.sql.Date) reservationVO.getDate()).toLocalDate()
					: null;
			prepareFormDropLists(model, guests, localDate);
			return "user/restaurant/booking/add";
		}
	}
	
	@PostMapping("/cancel")
	public String userCancelBooking(@RequestParam("reservationId") Integer reservationId, 
	                                HttpSession session, 
	                                RedirectAttributes redirectAttributes) {
		
		// 檢查登入狀態
		MemberVO loginMember = getCurrentMember(session);
		if (loginMember == null) {
			return "redirect:/login.html" + "?redirect=/restaurant/booking/list";
		}

		try {
			// 呼叫先前寫好、會自動連動「回復桌數」與「發送通知」的 service 方法
			reservationService.cancelReservation(reservationId);
			
			redirectAttributes.addFlashAttribute("successMessage", "您的訂位已成功取消，桌位已釋出。");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "取消失敗：" + e.getMessage());
		}

		// 取消完成後，重導向回原本的我的訂位紀錄清單
		return "redirect:/restaurant/booking/list";
	}

	private MemberVO getCurrentMember(HttpSession session) {
		MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");

		if (loginMember == null || loginMember.getMemberId() == null) {
			return null;
		}

		return memberAuthService.getMemberById(loginMember.getMemberId());
	}

	private void prepareFormDropLists(Model model, int guests, LocalDate selectedDate) {
		List<LocalDate> availableDates = availableTableService.getAvailableDatesByGuests(guests);
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