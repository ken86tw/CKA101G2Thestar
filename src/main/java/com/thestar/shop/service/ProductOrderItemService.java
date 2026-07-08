package com.thestar.shop.service;

import com.thestar.shop.entity.ProductOrderItemVO;
import com.thestar.shop.repository.ProductOrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductOrderItemService {

    @Autowired
    ProductOrderItemRepository repository;

    public void addProductOrderItem(ProductOrderItemVO productOrderItemVO) {
        repository.save(productOrderItemVO);
    }

    public void updateProductOrderItem(ProductOrderItemVO productOrderItemVO) {
        repository.save(productOrderItemVO);
    }

    public void deleteProductOrderItem(Integer productOrderItemId) {
        if (repository.existsById(productOrderItemId))
            repository.deleteByProductOrderItemId(productOrderItemId);
    }

    public ProductOrderItemVO getOneProductOrderItem(Integer productOrderItemId) {
        Optional<ProductOrderItemVO> optional = repository.findById(productOrderItemId);
        return optional.orElse(null);
    }

    public List<ProductOrderItemVO> getAll() {
        return repository.findAll();
    }

    public List<ProductOrderItemVO> getByShopOrderId(Integer shopOrderId) {
        return repository.findByShopOrderId(shopOrderId);
    }
}