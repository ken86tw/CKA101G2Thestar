package com.thestar.shop.repository;

import com.thestar.shop.entity.CartItemVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItemVO, Integer> {

    @Transactional
    @Modifying
    @Query(value = "delete from CART_ITEM where cart_item_id = ?1", nativeQuery = true)
    void deleteByCartItemId(int cartItemId);

    List<CartItemVO> findByMemberId(Integer memberId);
    
    CartItemVO findByMemberIdAndProductId(Integer memberId, Integer productId);
}