package com.thestar.restaurant.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thestar.restaurant.entity.RestaurantMenuVO;
import com.thestar.restaurant.repository.RestaurantMenuRepository;

@Service
public class RestaurantMenuService {

    @Autowired
    RestaurantMenuRepository repository;

    public void addRestaurantMenu(RestaurantMenuVO restaurantMenuVO) {
        repository.save(restaurantMenuVO);
    }

    public void updateRestaurantMenu(RestaurantMenuVO restaurantMenuVO) {
        repository.save(restaurantMenuVO);
    }

    public void deleteRestaurantMenu(Integer itemId) {
        if (repository.existsById(itemId))
            repository.deleteById(itemId);
    }

    public RestaurantMenuVO getOneRestaurantMenu(Integer itemId) {
        Optional<RestaurantMenuVO> optional = repository.findById(itemId);
        return optional.orElse(null);
    }

    // 取得全部，依分類順序 + 餐點順序排列（後台管理）
    public List<RestaurantMenuVO> getAll() {
        return repository.findAllOrderByCategoryAndSort();
    }

    // 依分類查詢（前台菜單頁）
    public List<RestaurantMenuVO> getByCategory(Integer categoryId) {
        return repository.findByMenuCategoryVO_CategoryIdOrderBySortOrderAsc(categoryId);
    }

    // 依餐點名稱模糊搜尋
    public List<RestaurantMenuVO> getByKeyword(String keyword) {
        return repository.findByItemNameLike("%" + keyword + "%");
    }

    // 刪除某分類下所有餐點（整批下架）
    public void deleteByCategory(Integer categoryId) {
        repository.deleteByCategoryId(categoryId);
    }
}
