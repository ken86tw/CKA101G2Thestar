package com.thestar.restaurant.controller.user;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
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
import com.thestar.restaurant.dto.SessionStatusDTO;
import com.thestar.restaurant.entity.AvailableTableVO;
import com.thestar.restaurant.entity.BusinessHoursVO; // 👈 修正 2：補上 BusinessHoursVO 的匯入
import com.thestar.restaurant.entity.ReservationStatus;
import com.thestar.restaurant.entity.RestaurantReservationVO;
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

	@GetMapping("/list")
	public String listMemberBookings(Model model, HttpSession session) {
		MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
		if (loginMember == null) {
			return "redirect:/login.html" + "?redirect=/restaurant/booking/list";
		}
		Integer currentMemberId = loginMember.getMemberId();

		List<RestaurantReservationVO> myReservations = reservationService.getByMemberId(currentMemberId);

		java.util.Map<Integer, com.thestar.restaurant.entity.RestaurantReviewVO> reviewMap = new java.util.HashMap<>();

		for (RestaurantReservationVO res : myReservations) {
			com.thestar.restaurant.entity.RestaurantReviewVO review = reviewService
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
	public List<SessionStatusDTO> getSessionStatus(
			@RequestParam("guests") int guests,
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		
		List<BusinessHoursVO> allSessions = businessHoursService.getAll();
		
		List<AvailableTableVO> availableTables = availableTableService.getAvailableSessionsByDateAndGuests(date, guests);
		
		java.util.Set<Integer> availableSessionIds = availableTables.stream()
				.map(table -> table.getId().getSessionId())
				.collect(Collectors.toSet());
		
		return allSessions.stream()
				.map(session -> {
					boolean isAvailable = availableSessionIds.contains(session.getSessionId());
					return new SessionStatusDTO(
							session.getSessionId(), 
							session.getStartTime(), 
							session.getEndTime(), 
							isAvailable
					);
				})
				.collect(Collectors.toList());
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
			model.addAttribute("errorMessage", "訂位人數不符合規範（限制 1-10 人）。");

			LocalDate localDate = (reservationVO.getDate() != null)
					? ((java.sql.Date) reservationVO.getDate()).toLocalDate()
					: null;
			prepareFormDropLists(model, guests, localDate);
			return "user/restaurant/booking";
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
			model.addAttribute("errorMessage", "輸入資料有誤，請檢查欄位。");
			LocalDate localDate = (reservationVO.getDate() != null)
					? ((java.sql.Date) reservationVO.getDate()).toLocalDate()
					: null;
			prepareFormDropLists(model, guests, localDate);
			return "user/restaurant/booking";
		}

		try {
			reservationService.addReservation(reservationVO);

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
			LocalDate localDate = (reservationVO.getDate() != null)
					? ((java.sql.Date) reservationVO.getDate()).toLocalDate()
					: null;
			prepareFormDropLists(model, guests, localDate);
			return "user/restaurant/booking";
		}
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