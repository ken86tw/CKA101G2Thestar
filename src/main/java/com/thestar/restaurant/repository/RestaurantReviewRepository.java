package com.thestar.restaurant.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.thestar.restaurant.entity.RestaurantReviewVO;

// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/
public interface RestaurantReviewRepository extends JpaRepository<RestaurantReviewVO, Integer> {

    // 查詢某訂位的評論（一筆訂位對應一筆評論）
    RestaurantReviewVO findByRestaurantReservationVO_ReservationId(Integer reservationId);

    // 查詢某會員的所有評論
    @Query("from RestaurantReviewVO where memberVO = ?1 order by reviewId desc")
    List<RestaurantReviewVO> findByMemberId(Integer memberId);

    // 取得所有評論，依評論 ID 降序（後台管理）
    @Query("from RestaurantReviewVO order by reviewId desc")
    List<RestaurantReviewVO> findAllOrderByReviewIdDesc();

    // 查詢星等 >= 指定值的評論（例如只撈 4 星以上正評）
    @Query("from RestaurantReviewVO where reviewStars >= ?1 order by reviewId desc")
    List<RestaurantReviewVO> findByMinStars(int minStars);

    // 計算平均星等（首頁展示用）
    @Query("select avg(r.reviewStars) from RestaurantReviewVO r")
    Double findAverageStars();
}
