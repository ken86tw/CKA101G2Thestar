package com.thestar.restaurant.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thestar.member.entity.MemberVO;
import com.thestar.restaurant.entity.RestaurantReservationVO;
import com.thestar.restaurant.entity.RestaurantReviewVO;
import com.thestar.restaurant.repository.RestaurantReservationRepository;
import com.thestar.restaurant.repository.RestaurantReviewRepository;

import jakarta.transaction.Transactional;

@Service
public class RestaurantReviewService {

    @Autowired
    RestaurantReviewRepository restaurantReviewRepository;
    
    @Autowired
    RestaurantReservationRepository restaurantReservationRepository;
    
    

    @Transactional // 涉及到多張表的更新，務必加上事務控制
    public void addReview(Integer memberId, Integer reservationId, Integer reviewStars, String reviewContent) {
        
        // 1. 建立並設定 RestaurantReviewVO
        RestaurantReviewVO reviewVO = new RestaurantReviewVO();
        reviewVO.setReviewStars(reviewStars);
        reviewVO.setReviewContent(reviewContent);

        // 設定關聯的 MemberVO (只需要塞入 ID 即可，JPA 會自動對應外鍵)
        MemberVO member = new MemberVO();
        member.setMemberId(memberId);
        reviewVO.setMemberVO(member);

        // 設定關聯的 RestaurantReservationVO
        RestaurantReservationVO reservation = new RestaurantReservationVO();
        reservation.setReservationId(reservationId);
        reviewVO.setRestaurantReservationVO(reservation);

        // 2. 儲存評論到資料庫
        restaurantReviewRepository.save(reviewVO);

        // 3. 同步把該筆訂位的 reviewStatus 改為 false 
        // 這裡直接借用你原本 Repository 的 update 概念，或寫一個專用的修改
        if (restaurantReservationRepository.existsById(reservationId)) {
            // 查出該筆訂位
            RestaurantReservationVO originalReservation = restaurantReservationRepository.findById(reservationId).orElseThrow();
            // 改成 false 代表已經評論過、關閉權限
            originalReservation.setReviewStatus(true); 
            // 存回去更新
            restaurantReservationRepository.save(originalReservation);
        }
    }

    public void updateReview(RestaurantReviewVO reviewVO) {
        restaurantReviewRepository.save(reviewVO);
    }

    public void deleteReview(Integer reviewId) {
        if (restaurantReviewRepository.existsById(reviewId))
            restaurantReviewRepository.deleteById(reviewId);
    }

    public RestaurantReviewVO getOneReview(Integer reviewId) {
        Optional<RestaurantReviewVO> optional = restaurantReviewRepository.findById(reviewId);
        return optional.orElse(null);
    }

    // 取得全部評論（後台管理）
    public List<RestaurantReviewVO> getAll() {
        return restaurantReviewRepository.findAllOrderByReviewIdDesc();
    }

    // 查某會員的所有評論
    public List<RestaurantReviewVO> getByMemberId(Integer memberId) {
        return restaurantReviewRepository.findByMemberId(memberId);
    }

    // 查某筆訂位的評論（確認是否已評論過）
    public RestaurantReviewVO getByReservationId(Integer reservationId) {
        return restaurantReviewRepository.findByRestaurantReservationVO_ReservationId(reservationId);
    }

    // 查高星評論（首頁精選）
    public List<RestaurantReviewVO> getHighStarReviews(int minStars) {
        return restaurantReviewRepository.findByMinStars(minStars);
    }

    // 取得平均星等（首頁展示）
    public Double getAverageStars() {
        return restaurantReviewRepository.findAverageStars();
    }
}
