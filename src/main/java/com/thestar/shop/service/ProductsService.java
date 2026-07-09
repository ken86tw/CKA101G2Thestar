package com.thestar.shop.service;

import com.thestar.shop.entity.ProductsVO;
import com.thestar.shop.repository.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductsService {

    @Autowired
    ProductsRepository repository;

    public void addProduct(ProductsVO productsVO) {
        productsVO.setProductReviewNumber(0);
        productsVO.setProductTotalStar(0);
        repository.save(productsVO);
    }

    public void updateProduct(ProductsVO productsVO) {
        repository.save(productsVO);
    }

    public void deleteProduct(Integer productId) {
        if (repository.existsById(productId))
            repository.deleteByProductId(productId);
    }

    public ProductsVO getOneProduct(Integer productId) {
        Optional<ProductsVO> optional = repository.findById(productId);
        return optional.orElse(null);
    }

    public List<ProductsVO> getAll() {
        return repository.findAll();
    }
    
    public List<ProductsVO> getAllByStatus(Byte productStatus) {
        return repository.findByProductStatus(productStatus);
    }
    
    public List<ProductsVO> getAllByStatusAndCategory(Byte productStatus, Integer productCategoryId) {
        return repository.findByProductStatusAndProductCategoryId(productStatus, productCategoryId);
    }
}