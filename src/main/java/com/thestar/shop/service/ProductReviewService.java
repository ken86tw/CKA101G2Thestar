package com.thestar.shop.service;

import com.thestar.shop.entity.ProductReviewVO;
import com.thestar.shop.repository.ProductReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
}