package com.thestar.shop.repository;

import com.thestar.shop.entity.ProductImageVO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImageVO, Integer> {

	List<ProductImageVO> findByProductId(Integer productId);

	ProductImageVO findFirstByProductIdAndIsCover(Integer productId, Byte isCover);

	// 設定新的封面圖片
	@Transactional
	@Modifying
	@Query(value = """
			UPDATE PRODUCT_IMAGE
			SET IS_COVER = 1
			WHERE PRODUCT_IMAGE_ID = ?1
			""", nativeQuery = true)
	void updateCover(Integer productImageId);
	
	// 清除某商品的所有封面設定
	@Transactional
	@Modifying
	@Query(value = "UPDATE PRODUCT_IMAGE SET IS_COVER = 0 WHERE PRODUCT_ID = ?1", nativeQuery = true)
	void clearCoverByProductId(Integer productId);

}