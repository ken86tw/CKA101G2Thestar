package com.thestar.shop.service;

import com.thestar.shop.entity.ShopOrderVO;
import com.thestar.shop.repository.ShopOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShopOrderService {

    @Autowired
    ShopOrderRepository repository;

    public void addShopOrder(ShopOrderVO shopOrderVO) {
        repository.save(shopOrderVO);
    }

    public void updateShopOrder(ShopOrderVO shopOrderVO) {
        repository.save(shopOrderVO);
    }

    public void deleteShopOrder(Integer shopOrderId) {
        if (repository.existsById(shopOrderId))
            repository.deleteByShopOrderId(shopOrderId);
    }

    public ShopOrderVO getOneShopOrder(Integer shopOrderId) {
        Optional<ShopOrderVO> optional = repository.findById(shopOrderId);
        return optional.orElse(null);
    }

    public List<ShopOrderVO> getAll() {
        return repository.findAll();
    }

    public List<ShopOrderVO> getByMemberId(Integer memberId) {
        return repository.findByMemberId(memberId);
    }
}