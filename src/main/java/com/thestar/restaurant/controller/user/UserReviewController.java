package com.thestar.restaurant.controller.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thestar.member.entity.MemberVO;
import com.thestar.member.service.MemberService;
import com.thestar.restaurant.entity.RestaurantReservationVO;
import com.thestar.restaurant.entity.RestaurantReviewVO;
import com.thestar.restaurant.service.RestaurantReservationService;
import com.thestar.restaurant.service.RestaurantReviewService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/restaurant")
public class UserReviewController {
	
    @Autowired
    private RestaurantReviewService reviewService;
    
    @Autowired
    private MemberService memberService;
    
    @Autowired
    private RestaurantReservationService restaurantReservationService;
    
    Integer memberId = 1;
    
    
	
    @GetMapping("/reviews")
    public String reviews(Model model) {
        List<RestaurantReviewVO> reviewList = reviewService.getAll();
        Double averageStars = reviewService.getAverageStars();
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("averageStars", averageStars != null ? averageStars : 0.0);
        return "user/review/list";
    }
    
    @GetMapping("/reviews/add")
    public String addReviewPage(HttpSession session, Model model) {
        // 1. 從 Session 取得當前登入的會員 ID
        
        
        // 安全機制：沒登入就踢去登入頁
        if (memberId == null) {
            return "redirect:/login"; 
        }

        // 2. 撈取會員資料（為了對應 HTML 的 ${member.memberName}）
        MemberVO member = memberService.getMemberById(memberId);
        model.addAttribute("member", member);

        // 3. 撈取該會員「已完成、未評論」的訂位紀錄（為了對應 HTML 的 ${reservations}）
        List<RestaurantReservationVO> reservations = restaurantReservationService.getUnreviewedReservationsByMemberId(memberId);
        model.addAttribute("reservations", reservations);

        // 4. 回傳 Thymeleaf 頁面路徑 (位於 templates/restaurant/addReview.html)
        return "user/review/add"; 
    }
    
    @PostMapping("/submitReview")
    public String submitReview(
            HttpSession session,
            @RequestParam("reservationId") Integer reservationId,
            @RequestParam("reviewStars") Integer reviewStars,
            @RequestParam("reviewContent") String reviewContent) {

        // 安全機制
        
        if (memberId == null) {
            return "redirect:/login";
        }

        // 執行商業邏輯：
        // 1. 新增一筆 Review 紀錄到資料庫
        // 2. 把該筆訂位的 reviewStatus 改為 false (代表已評論，從此消失在下拉選單中)
        reviewService.addReview(memberId, reservationId, reviewStars, reviewContent);

        // 評論成功後，重定向回評論列表
        return "redirect:/restaurant/reviews";
    }
    
    
    

}
