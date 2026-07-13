package com.thestar.restaurant.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.thestar.restaurant.entity.RestaurantReservationVO;
import com.thestar.restaurant.entity.ReservationStatus;
import com.thestar.restaurant.service.RestaurantReservationService;
import java.util.List;

@Controller
@RequestMapping("/admin/restaurant/reservation")
public class RestaurantReservationController {

    @Autowired
    private RestaurantReservationService reservationService;

    // 預約紀錄列表
    @GetMapping("/list")
    public String list(Model model) {
        List<RestaurantReservationVO> list = reservationService.getAll();
        model.addAttribute("reservationList", list);
        return "admin/restaurant/reservation/list";
    }

    // 變更預約狀態 (例如取消或完成)
    @PostMapping("/updateStatus")
    public String updateStatus(@RequestParam("reservationId") Integer reservationId, 
                               @RequestParam("status") String status) {
        if ("CANCELED".equals(status)) {
            reservationService.cancelReservation(reservationId);
        } else if ("FINISHED".equals(status)) {
            reservationService.finishReservation(reservationId);
        }
        return "redirect:/admin/restaurant/reservation/list";
    }
}