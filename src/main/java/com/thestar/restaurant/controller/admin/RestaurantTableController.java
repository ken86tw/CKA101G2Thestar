package com.thestar.restaurant.controller.admin;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.thestar.restaurant.entity.RestaurantTableVO;
import com.thestar.restaurant.service.AvailableTableService;
import com.thestar.restaurant.service.BusinessHoursService; // 💡 確保有匯入此 Service
import com.thestar.restaurant.service.RestaurantTableService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/restaurant/table")
public class RestaurantTableController {

    @Autowired
    private RestaurantTableService tableService;
    
    @Autowired
    private AvailableTableService availableTableService;

    @Autowired
    private BusinessHoursService businessHoursService; // 💡 注入營業時段 Service

    // 桌型列表
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("tableList", tableService.getAll());
        // 💡 將營業時段資料傳給前端供選擇器使用
        model.addAttribute("businessHoursList", businessHoursService.getAll()); 
        return "admin/restaurant/table/list";
    }

    // 處理新增或修改
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("restaurantTableVO") RestaurantTableVO tableVO, BindingResult result) {
        if (result.hasErrors()) {
            return "admin/restaurant/table/add";
        }
        tableService.updateRestaurantTable(tableVO);
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