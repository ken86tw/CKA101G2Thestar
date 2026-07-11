package com.thestar.shop.service;

import com.thestar.shop.entity.CartItemVO;
import com.thestar.shop.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartItemService {

    @Autowired
    CartItemRepository repository;

    public void addCartItem(CartItemVO cartItemVO) {
        repository.save(cartItemVO);
    }

    public void updateCartItem(CartItemVO cartItemVO) {
        repository.save(cartItemVO);
    }

    public void deleteCartItem(Integer cartItemId) {
        if (repository.existsById(cartItemId))
            repository.deleteByCartItemId(cartItemId);
    }

    public CartItemVO getOneCartItem(Integer cartItemId) {
        Optional<CartItemVO> optional = repository.findById(cartItemId);
        return optional.orElse(null);
    }

    public List<CartItemVO> getAll() {
        return repository.findAll();
    }

    public List<CartItemVO> getByMemberId(Integer memberId) {
        return repository.findByMemberId(memberId);
    }
    
    public CartItemVO getByMemberIdAndProductId(Integer memberId, Integer productId) {
        return repository.findByMemberIdAndProductId(memberId, productId);
    }
}