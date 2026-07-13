package com.thestar.restaurant.controller.user;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.thestar.restaurant.entity.RestaurantMenuVO;
import com.thestar.restaurant.entity.RestaurantReservationVO;
import com.thestar.restaurant.entity.RestaurantReviewVO;
import com.thestar.member.entity.MemberVO;
import com.thestar.restaurant.entity.ReservationStatus; // 確保有匯入您的狀態 Enum
import com.thestar.restaurant.service.RestaurantMenuService;
import com.thestar.restaurant.service.RestaurantReservationService;
import com.thestar.restaurant.service.RestaurantReviewService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/restaurant")
public class RestaurantUserController {

    @Autowired
    private RestaurantMenuService menuService;

    @Autowired
    private RestaurantReservationService reservationService;



    @GetMapping({"", "/", "/index"})
    public String index() {
        return "user/restaurant/index";
    }

    @GetMapping("/menu")
    public String menu(Model model) {
        List<RestaurantMenuVO> menuList = menuService.getAll();
        model.addAttribute("menuList", menuList);
        return "user/restaurant/menu";
    }



    
}