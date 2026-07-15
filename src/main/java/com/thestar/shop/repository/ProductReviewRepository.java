package com.thestar.shop.repository;

import com.thestar.shop.entity.ProductReviewVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductReviewRepository extends JpaRepository<ProductReviewVO, Integer> {

    @Transactional
    @Modifying
    @Query(value = "delete from product_review where product_review_id = ?1", nativeQuery = true)
    void deleteByProductReviewId(int productReviewId);

    List<ProductReviewVO> findByProductId(Integer productId);
    List<ProductReviewVO> findByMemberId(Integer memberId);
    
    boolean existsByProductOrderItemId(Integer productOrderItemId);
}