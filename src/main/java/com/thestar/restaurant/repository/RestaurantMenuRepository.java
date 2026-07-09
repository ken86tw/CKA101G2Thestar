package com.thestar.restaurant.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.thestar.restaurant.entity.RestaurantMenuVO;

// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/
public interface RestaurantMenuRepository extends JpaRepository<RestaurantMenuVO, Integer> {

    // 查詢某分類下所有餐點，依排序顯示（前台菜單）
    // menuCategoryVO_categoryId → 關聯物件 menuCategoryVO 裡的 categoryId 欄位
    List<RestaurantMenuVO> findByMenuCategoryVO_CategoryIdOrderBySortOrderAsc(Integer categoryId);

    // 餐點名稱模糊搜尋
    @Query("from RestaurantMenuVO where itemName like ?1 order by sortOrder")
    List<RestaurantMenuVO> findByItemNameLike(String keyword);

    // 取得所有餐點，依分類排序再依餐點排序（後台管理用）
    @Query("from RestaurantMenuVO order by menuCategoryVO.sortOrder, sortOrder")
    List<RestaurantMenuVO> findAllOrderByCategoryAndSort();

    // 刪除某分類下的所有餐點（刪分類前先清餐點）
    @Transactional
    @Modifying
    @Query(value = "delete from RESTAURANT_MENU where CATEGORY_ID = ?1", nativeQuery = true)
    void deleteByCategoryId(int categoryId);
}
