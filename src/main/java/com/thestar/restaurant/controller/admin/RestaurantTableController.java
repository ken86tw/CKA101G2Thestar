package com.thestar.restaurant.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.thestar.restaurant.entity.RestaurantTableVO;
import com.thestar.restaurant.service.RestaurantTableService;
import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/admin/restaurant/table")
public class RestaurantTableController {

    @Autowired
    private RestaurantTableService tableService;

    // 桌型列表
    @GetMapping("/list")
    public String list(Model model) {
        List<RestaurantTableVO> list = tableService.getAll();
        model.addAttribute("tableList", list);
        return "admin/restaurant/table/list";
    }


    // 處理新增或修改
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("restaurantTableVO") RestaurantTableVO tableVO, BindingResult result) {
        if (result.hasErrors()) {
            return "admin/restaurant/table/add";
        }
        tableService.updateRestaurantTable(tableVO); // save方法兼具新增與修改
        return "redirect:/admin/restaurant/table/list";
    }

    // 前往修改頁面
    @GetMapping("/editPage")
    public String editPage(@RequestParam("tableType") Integer tableType, Model model) {
        RestaurantTableVO tableVO = tableService.getOneRestaurantTable(tableType);
        model.addAttribute("restaurantTableVO", tableVO);
        return "admin/restaurant/table/edit";
    }

    // 刪除桌型
    @PostMapping("/delete")
    public String delete(@RequestParam("tableType") Integer tableType) {
        tableService.deleteRestaurantTable(tableType);
        return "redirect:/admin/restaurant/table/list";
    }
}