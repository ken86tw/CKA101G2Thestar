package com.thestar.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thestar.restaurant.entity.RestaurantTableVO;

// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/
public interface RestaurantTableRepository extends JpaRepository<RestaurantTableVO, Integer> {
    // 桌型資料較簡單，繼承的 findAll() / findById() / save() / deleteById() 即可應付大部分需求
}
