package com.thestar.restaurant.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thestar.restaurant.entity.MenuCategoryVO;
import com.thestar.restaurant.repository.MenuCategoryRepository;

@Service
public class MenuCategoryService {

    @Autowired
    MenuCategoryRepository repository;

    public void addMenuCategory(MenuCategoryVO menuCategoryVO) {
        repository.save(menuCategoryVO);
    }

    public void updateMenuCategory(MenuCategoryVO menuCategoryVO) {
        repository.save(menuCategoryVO);
    }

    public void deleteMenuCategory(Integer categoryId) {
        if (repository.existsById(categoryId))
            repository.deleteById(categoryId);
    }

    public MenuCategoryVO getOneMenuCategory(Integer categoryId) {
        Optional<MenuCategoryVO> optional = repository.findById(categoryId);
        return optional.orElse(null);
    }

    // 前台菜單顯示用：依 SORT_ORDER 排序
    public List<MenuCategoryVO> getAll() {
        return repository.findAllByOrderBySortOrderAsc();
    }

    // 後台關鍵字搜尋（模糊查詢要自行加 %，例如："%飲%"）
    public List<MenuCategoryVO> getAll(String keyword) {
        return repository.findByCategoryNameLike("%" + keyword + "%");
    }
}
