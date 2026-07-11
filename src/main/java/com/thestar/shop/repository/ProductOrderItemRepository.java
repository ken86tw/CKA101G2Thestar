package com.thestar.shop.repository;

import com.thestar.shop.entity.ProductOrderItemVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductOrderItemRepository extends JpaRepository<ProductOrderItemVO, Integer> {

    @Transactional
    @Modifying
    @Query(value = "delete from product_order_item where product_order_item_id = ?1", nativeQuery = true)
    void deleteByProductOrderItemId(int productOrderItemId);

    List<ProductOrderItemVO> findByShopOrderId(Integer shopOrderId);
}