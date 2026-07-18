package com.thestar.restaurant.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thestar.member.service.MemberNotifyService;
import com.thestar.restaurant.entity.BusinessHoursVO;
import com.thestar.restaurant.entity.ReservationStatus;
import com.thestar.restaurant.entity.RestaurantReservationVO;
import com.thestar.restaurant.repository.RestaurantReservationRepository;

import jakarta.mail.Session;

@Service
public class RestaurantReservationService {

	@Autowired
	private RestaurantReservationRepository repository;

	@Autowired
	private BusinessHoursService businessHoursService;

	@Autowired
	private AvailableTableService availableTableService;

	@Autowired
	private MemberNotifyService memberNotifyService;

	@Transactional
	public void addReservation(RestaurantReservationVO reservationVO) {
		repository.save(reservationVO);
	}

	public void updateReservation(RestaurantReservationVO reservationVO) {
		repository.save(reservationVO);
	}

	public void deleteReservation(Integer reservationId) {
		if (repository.existsById(reservationId))
			repository.deleteById(reservationId);
	}

	public RestaurantReservationVO getOneReservation(Integer reservationId) {
		Optional<RestaurantReservationVO> optional = repository.findById(reservationId);
		return optional.orElse(null);
	}

	public List<RestaurantReservationVO> getAll() {
		return repository.findAll();
	}

	// 查某會員的所有訂位
	public List<RestaurantReservationVO> getByMemberId(Integer memberId) {
		return repository.findByMemberId(memberId);
	}

	// 查某天所有訂位（後台當日總覽）
	public List<RestaurantReservationVO> getByDate(Date date) {
		return repository.findByDateOrderByBusinessHoursVO_StartTimeAsc(date);
	}

	// 查某天某時段的訂位
	public List<RestaurantReservationVO> getByDateAndSession(Date date, Integer sessionId) {
		return repository.findByDateAndSession(date, sessionId);
	}

	// 查某會員特定狀態的訂位
	public List<RestaurantReservationVO> getByMemberIdAndStatus(Integer memberId, ReservationStatus status) {
		return repository.findByMemberIdAndStatus(memberId, status);
	}

	// 取消訂位
	@Transactional // 👈 記得加上事務註解，確保預約狀態更新與桌數回復是一致的
	public void cancelReservation(Integer reservationId) {
		// 1. 先把整筆預約單撈出來，因為我們需要裡面的「日期」、「時段(Session)」與「桌型」來加回數量
		RestaurantReservationVO res = repository.findById(reservationId).orElse(null);

		if (res != null) {
			// 2. 執行原本的預約狀態更新 (改為 CANCELED)
			repository.cancelReservation(reservationId);

			String timeRange = "未知時段";
			LocalDate resDate = res.getDate().toLocalDate();
			;
			Integer sessionId = res.getBusinessHoursVO().getSessionId(); // 假設對應欄位是 sessionId
			try {
				BusinessHoursVO bh = businessHoursService.getOneBusinessHours(sessionId);
				if (bh != null && bh.getStartTime() != null && bh.getEndTime() != null) {
					timeRange = bh.getStartTime() + " ~ " + bh.getEndTime();
				}
			} catch (Exception e) {
				timeRange = "時段編號 " + sessionId;
			}

			// 4. 組裝「取消預約」的通知內容 (把成功改為取消)
			// TODO: 等你提供人數欄位後，我會把 res.getXXX() 補在最後面！
			String notificationContent = "【The Star 餐廳】您在日期：" + resDate + "，時段：" + timeRange + "的餐廳訂位已取消!";

			// 3. 連動回復桌數邏輯
			if (res.getDate() != null && res.getBusinessHoursVO() != null && res.getRestaurantTableVO() != null) {

				String tableTypeName = res.getRestaurantTableVO().getTableTypeName(); // 取得大桌或小桌名稱
				if (res.getMemberVO() != null) {
                    memberNotifyService.createNotification(res.getMemberVO().getMemberId(), notificationContent);
                }
				
				// 根據前端畫面的顯示判定：如果綁定的是大桌就加回大桌，小桌就加回小桌
				// 💡 提示：若你實體內是用 ID 區分 (1是大桌, 2是小桌)，也可以改成用 ID 判斷
				if ("LARGE_TABLE".equalsIgnoreCase(tableTypeName)) {
					availableTableService.restoreLargeTableCount(resDate, sessionId);
				} else if ("SMALL_TABLE".equalsIgnoreCase(tableTypeName)) {
					availableTableService.restoreSmallTableCount(resDate, sessionId);
				}
			}
		}
	}

	// 完成訂位並開放評論（結帳時呼叫）
	public void finishReservation(Integer reservationId) {
		if (repository.existsById(reservationId)) {
			repository.finishReservation(reservationId);
		}
		RestaurantReservationVO res = repository.findById(reservationId).orElse(null);

		if (res.getDate() != null && res.getBusinessHoursVO() != null) {
			LocalDate resDate = res.getDate().toLocalDate();
			Integer sessionId = res.getBusinessHoursVO().getSessionId();

			String timeRange = "未知時段";
			try {
				BusinessHoursVO bh = businessHoursService.getOneBusinessHours(sessionId);
				if (bh != null && bh.getStartTime() != null && bh.getEndTime() != null) {
					timeRange = bh.getStartTime() + " ~ " + bh.getEndTime();
				}
			} catch (Exception e) {
				timeRange = "時段編號 " + sessionId;
			}

			String notificationContent = "【The Star 餐廳】感謝您前來就餐！\n" + "您於 " + resDate + " " + timeRange + "的用餐已完成帶位。\n"
					+ "美味值得被記錄，誠摯邀請您前往「我的訂位紀錄」為本次用餐留下五星好評！⭐";

			if (res.getMemberVO() != null) {
				memberNotifyService.createNotification(res.getMemberVO().getMemberId(), notificationContent);
			}
		}
	}

	// 查某會員「已完成」且「尚未評論」的訂位紀錄
	public List<RestaurantReservationVO> getUnreviewedReservationsByMemberId(Integer memberId) {
		// 1. 先定義什麼狀態叫「已完成」用餐（假設你的列舉叫 ReservationStatus.FINISHED）
		ReservationStatus status = ReservationStatus.FINISHED;

		// 2. 呼叫剛剛在 Repository 寫好的方法（這邊以「尚未評論 hasReviewed = false」為例）
		return repository.findUnreviewedReservations(memberId, status);
	}
}
