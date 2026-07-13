package com.thestar.restaurant.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.thestar.restaurant.entity.RestaurantMenuVO;
import com.thestar.restaurant.service.RestaurantMenuService;
import com.thestar.restaurant.service.MenuCategoryService;
import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/admin/restaurant/menu")
public class RestaurantMenuController {

    @Autowired
    private RestaurantMenuService menuService;

    @Autowired
    private MenuCategoryService categoryService;

    // 菜單與餐點列表
    @GetMapping("/list")
    public String list(Model model) {
        List<RestaurantMenuVO> list = menuService.getAll();
        model.addAttribute("menuList", list);
        return "admin/restaurant/menu/list";
    }

    // 前往新增餐點
    @GetMapping("/addPage")
    public String addPage(Model model) {
        model.addAttribute("restaurantMenuVO", new RestaurantMenuVO());
        model.addAttribute("categoryList", categoryService.getAll()); // 下拉選單供選擇分類
        return "admin/restaurant/menu/add";
    }

    // 儲存餐點
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("restaurantMenuVO") RestaurantMenuVO menuVO, 
                       BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categoryList", categoryService.getAll());
            return "admin/restaurant/menu/add";
        }
        menuService.addRestaurantMenu(menuVO);
        return "redirect:/admin/restaurant/menu/list";
    }

    // 前往修改餐點
    @GetMapping("/editPage")
    public String editPage(@RequestParam("itemId") Integer itemId, Model model) {
        RestaurantMenuVO menuVO = menuService.getOneRestaurantMenu(itemId);
        model.addAttribute("restaurantMenuVO", menuVO);
        model.addAttribute("categoryList", categoryService.getAll());
        return "admin/restaurant/menu/edit";
    }

    // 刪除餐點
    @PostMapping("/delete")
    public String delete(@RequestParam("itemId") Integer itemId) {
        menuService.deleteRestaurantMenu(itemId);
        return "redirect:/admin/restaurant/menu/list";
    }
}