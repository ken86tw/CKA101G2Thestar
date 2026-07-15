package com.thestar.shop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thestar.shop.entity.ProductReviewVO;
import com.thestar.shop.entity.ProductsVO;
import com.thestar.shop.repository.ProductReviewRepository;

@Service
public class ProductReviewService {

    @Autowired
    ProductReviewRepository repository;

    public void addProductReview(ProductReviewVO productReviewVO) {
        repository.save(productReviewVO);
    }

    public void updateProductReview(ProductReviewVO productReviewVO) {
        repository.save(productReviewVO);
    }

    public void deleteProductReview(Integer productReviewId) {
        if (repository.existsById(productReviewId))
            repository.deleteByProductReviewId(productReviewId);
    }

    public ProductReviewVO getOneProductReview(Integer productReviewId) {
        Optional<ProductReviewVO> optional = repository.findById(productReviewId);
        return optional.orElse(null);
    }

    public List<ProductReviewVO> getAll() {
        return repository.findAll();
    }

    public List<ProductReviewVO> getByProductId(Integer productId) {
        return repository.findByProductId(productId);
    }

    public List<ProductReviewVO> getByMemberId(Integer memberId) {
        return repository.findByMemberId(memberId);
    }
    
    public boolean existsByProductOrderItemId(Integer productOrderItemId) {
        return repository.existsByProductOrderItemId(productOrderItemId);
    }

    public void updateAverageRating(ProductsVO product, Byte newRate) {
        int totalStar = (product.getProductTotalStar() == null ? 0 : product.getProductTotalStar()) + newRate;
        int reviewNumber = (product.getProductReviewNumber() == null ? 0 : product.getProductReviewNumber()) + 1;
        product.setProductTotalStar(totalStar);
        product.setProductReviewNumber(reviewNumber);
    }	
}