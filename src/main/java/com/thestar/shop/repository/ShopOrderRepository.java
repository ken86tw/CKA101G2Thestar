package com.thestar.shop.repository;

import com.thestar.shop.entity.ShopOrderVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ShopOrderRepository extends JpaRepository<ShopOrderVO, Integer> {

    @Transactional
    @Modifying
    @Query(value = "delete from shop_order where shop_order_id = ?1", nativeQuery = true)
    void deleteByShopOrderId(int shopOrderId);

    List<ShopOrderVO> findByMemberId(Integer memberId);
}