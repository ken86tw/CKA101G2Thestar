package com.thestar.shop.repository;

import com.thestar.shop.entity.ProductCategoryVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategoryVO, Integer> {

    @Transactional
    @Modifying
    @Query(value = "delete from product_category where product_category_id = ?1", nativeQuery = true)
    void deleteByProductCategoryId(int productCategoryId);
}