package com.thestar.restaurant.repository;

import java.sql.Time;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.thestar.restaurant.entity.BusinessHoursVO;

// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/
public interface BusinessHoursRepository extends JpaRepository<BusinessHoursVO, Integer> {

    // 查詢在指定時間點「正在營業」的時段
    @Query("from BusinessHoursVO where startTime <= ?1 and endTime >= ?1")
    List<BusinessHoursVO> findActiveSession(Time currentTime);

    // 依開始時間排序取得所有時段
    @Query("from BusinessHoursVO order	 by startTime")
    List<BusinessHoursVO> findAllOrderByStartTime();
}
