package com.thestar.restaurant.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.thestar.restaurant.entity.MenuCategoryVO;

// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/
public interface MenuCategoryRepository extends JpaRepository<MenuCategoryVO, Integer> {

    // 依 SORT_ORDER 排序取得所有分類（前台菜單顯示用）
    // 方法命名查詢（不需要寫 @Query，Spring Data 自動產生 SQL）
    List<MenuCategoryVO> findAllByOrderBySortOrderAsc();

    // 以分類名稱模糊搜尋
    @Query("from MenuCategoryVO where categoryName like ?1 order by sortOrder")
    List<MenuCategoryVO> findByCategoryNameLike(String keyword);
}
