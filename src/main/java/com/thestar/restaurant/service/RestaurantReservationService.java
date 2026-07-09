package com.thestar.restaurant.service;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thestar.restaurant.entity.ReservationStatus;
import com.thestar.restaurant.entity.RestaurantReservationVO;
import com.thestar.restaurant.repository.RestaurantReservationRepository;

@Service
public class RestaurantReservationService {

    @Autowired
    RestaurantReservationRepository repository;

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
    public void cancelReservation(Integer reservationId) {
        if (repository.existsById(reservationId))
            repository.cancelReservation(reservationId);
    }

    // 完成訂位並開放評論（結帳時呼叫）
    public void finishReservation(Integer reservationId) {
        if (repository.existsById(reservationId)) {
            repository.finishReservation(reservationId);

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
