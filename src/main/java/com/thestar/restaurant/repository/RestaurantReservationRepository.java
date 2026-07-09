package com.thestar.restaurant.repository;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.thestar.restaurant.entity.ReservationStatus;
import com.thestar.restaurant.entity.RestaurantReservationVO;

public interface RestaurantReservationRepository extends JpaRepository<RestaurantReservationVO, Integer> {

    // 1. 查詢某會員的所有訂位紀錄（透過物件內的 memberId 屬性查詢）
    @Query("from RestaurantReservationVO r where r.memberVO.memberId = ?1 order by r.date desc")
    List<RestaurantReservationVO> findByMemberId(Integer memberId);

    // 2. 查詢某會員特定狀態的訂位
    @Query("from RestaurantReservationVO r where r.memberVO.memberId = ?1 and r.reservationStatus = ?2 order by r.date desc")
    List<RestaurantReservationVO> findByMemberIdAndStatus(Integer memberId, ReservationStatus status);

    // 3. 查詢某天的所有訂位（保持原樣，Spring Data JPA 方法命名查詢很棒）
    List<RestaurantReservationVO> findByDateOrderByBusinessHoursVO_StartTimeAsc(Date date);

    // 4. 查詢某天 + 某時段的訂位（建議加上別名 r 更嚴謹）
    @Query("from RestaurantReservationVO r where r.date = ?1 and r.businessHoursVO.sessionId = ?2")
    List<RestaurantReservationVO> findByDateAndSession(Date date, Integer sessionId);

    // 5. 取消訂位（更新狀態為 CANCELED）
    @Transactional
    @Modifying
    @Query("update RestaurantReservationVO r set r.reservationStatus = com.thestar.restaurant.entity.ReservationStatus.CANCELED where r.reservationId = ?1")
    void cancelReservation(int reservationId);

    // 6. 完成訂位（更新狀態為 FINISHED）
    @Transactional
    @Modifying
    @Query("update RestaurantReservationVO r set r.reservationStatus = com.thestar.restaurant.entity.ReservationStatus.FINISHED where r.reservationId = ?1")
    void finishReservation(int reservationId);

    
    @Query("from RestaurantReservationVO r where r.memberVO.memberId = ?1 and r.reservationStatus = ?2 and r.reviewStatus = false")
    List<RestaurantReservationVO> findUnreviewedReservations(Integer memberId, ReservationStatus status);
}