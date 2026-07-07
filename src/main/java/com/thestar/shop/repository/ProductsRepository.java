package com.thestar.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.thestar.shop.entity.ProductsVO;

public interface ProductsRepository extends JpaRepository<ProductsVO, Integer> {

    @Transactional
    @Modifying
    @Query(value = "delete from products where product_id = ?1", nativeQuery = true)
    void deleteByProductId(int productId);
    List<ProductsVO> findByProductStatus(Byte productStatus);
    
    List<ProductsVO> findByProductStatusAndProductCategoryId(Byte productStatus, Integer productCategoryId);
}