package com.thestar.shop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thestar.shop.entity.ProductCategoryVO;
import com.thestar.shop.repository.ProductCategoryRepository;

@Service
public class ProductCategoryService {

    @Autowired
    ProductCategoryRepository repository;
    
    public void addProductCategory(ProductCategoryVO productCategoryVO) {
        repository.save(productCategoryVO);
    }

    public void updateProductCategory(ProductCategoryVO productCategoryVO) {
        repository.save(productCategoryVO);
    }

    public void deleteProductCategory(Integer productCategoryId) {
        if (repository.existsById(productCategoryId))
            repository.deleteByProductCategoryId(productCategoryId);
    }

    public ProductCategoryVO getOneProductCategory(Integer productCategoryId) {
        Optional<ProductCategoryVO> optional = repository.findById(productCategoryId);
        return optional.orElse(null);
    }

    public List<ProductCategoryVO> getAll() {
        return repository.findAll();
    }
}