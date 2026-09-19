package com.thestar.restaurant.controller.admin;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.thestar.restaurant.entity.AvailableTableVO;
import com.thestar.restaurant.service.AvailableTableService;
import com.thestar.restaurant.service.BusinessHoursService;

@Controller
@RequestMapping("/admin/restaurant/table")
public class AvailableTableController {

    @Autowired
    private AvailableTableService availableTableService;
    
    @Autowired
    private BusinessHoursService businessHoursService;

    // 1. 查詢頁面：對應到 templates/admin/restaurant/availabletable/list.html
    @GetMapping("/availability")
    public String availabilityPage(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        model.addAttribute("checkDate", targetDate);
        model.addAttribute("tableList", availableTableService.getByDate(targetDate));
        model.addAttribute("businessHoursList", businessHoursService.getAll());
        
        return "admin/restaurant/availabletable/list";
    }

    // 2. 區間批次關閉訂位
    @PostMapping("/closeSessionRange")
    public String closeSessionRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("startSessionId") Integer startSessionId,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam("endSessionId") Integer endSessionId, 
            RedirectAttributes redirectAttributes) {
        
        try {
            availableTableService.closeAvailableTablesRange(startDate, startSessionId, endDate, endSessionId);
            redirectAttributes.addFlashAttribute("successMessage", "指定區間訂位已關閉，桌數已歸零。");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "系統錯誤，請稍後再試。");
        }
        
        // 導回查詢頁面並帶上起始日期
        return "redirect:/admin/restaurant/table/availability?date=" + startDate;
    }

    // 3. 單一時段手動調整
    @PostMapping("/updateSingle")
    public String updateSingle(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("sessionId") Integer sessionId, 
            @RequestParam("large") Integer large,
            @RequestParam("small") Integer small,
            RedirectAttributes redirectAttributes) {

        try {
            AvailableTableVO vo = availableTableService.getOneAvailableTable(date, sessionId);
            if (vo != null) {
                vo.setLargeTableCount(large);
                vo.setSmallTableCount(small);
                availableTableService.updateAvailableTable(vo);
                redirectAttributes.addFlashAttribute("successMessage", "單一時段桌數已更新。");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "更新失敗：" + e.getMessage());
        }
        
        return "redirect:/admin/restaurant/table/availability?date=" + date;
    }
}