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
    
//    Integer memberId = 1;
    
    
    
	
    @GetMapping("/review")
    public String reviews(Model model) {
        List<RestaurantReviewVO> reviewList = reviewService.getAll();
        Double averageStars = reviewService.getAverageStars();
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("averageStars", averageStars != null ? averageStars : 0.0);
        return "user/restaurant/review/list";
    }
    
    @GetMapping("/review/add")
    public String addReviewPage(
        @RequestParam(value = "resId", required = false) Integer reservationId, 
        HttpSession session, 
        Model model
    ) {
        // 1. 從 Session 取得當前登入的會員 ID
//        Integer memberId = 1; 
//        
//        if (memberId == null) {
//            return "redirect:/login"; 
//        }

    	//members
    	MemberVO member = getCurrentMember(session);
    	if (member == null) {
    	    return "redirect:/login.html" + "?redirect=/restaurant/review/add";
    	}
    	Integer memberId = member.getMemberId();

        // 2. 撈取會員資料
//        MemberVO member = memberService.getMemberById(memberId);
        model.addAttribute("member", member);

        // 3. 處理訂位紀錄邏輯
        if (reservationId != null) {
            // 【情況 A】有帶 ?resId=7
            RestaurantReservationVO singleReservation = restaurantReservationService.getOneReservation(reservationId);
            
            if (singleReservation != null) {
                // 包成 List 丟給前端，畫面上就只會出現這 1 筆
                model.addAttribute("reservations", List.of(singleReservation));
                model.addAttribute("isLocked", true); // 告訴前端：要把下拉選單鎖死
            } else {
                // 安全防護：如果網址的 ID 根本不存在，退回預設狀態
                model.addAttribute("reservations", restaurantReservationService.getUnreviewedReservationsByMemberId(memberId));
                model.addAttribute("isLocked", false);
            }
        } else {
            // 【情況 B】沒有帶參數，讓使用者自己選
            model.addAttribute("reservations", restaurantReservationService.getUnreviewedReservationsByMemberId(memberId));
            model.addAttribute("isLocked", false);
        }

        // 4. 回傳 Thymeleaf 頁面路徑
        return "user/restaurant/review/add"; 
    }
    
    @PostMapping("/submitReview")
    public String submitReview(
            HttpSession session,
            @RequestParam("reservationId") Integer reservationId,
            @RequestParam("reviewStars") Integer reviewStars,
            @RequestParam("reviewContent") String reviewContent) {

        // 安全機制
        
//        if (memberId == null) {
//            return "redirect:/login";
//        }
    	
    	//members
    	MemberVO member = getCurrentMember(session);
    	if (member == null) {return "redirect:/login.html" + "?redirect=/restaurant/review/add";}
    	Integer memberId = member.getMemberId();

        // 執行商業邏輯：
        // 1. 新增一筆 Review 紀錄到資料庫
        // 2. 把該筆訂位的 reviewStatus 改為 false (代表已評論，從此消失在下拉選單中)
        reviewService.addReview(memberId, reservationId, reviewStars, reviewContent);

        // 評論成功後，重定向回評論列表
        return "redirect:/restaurant/review";
    }
    
    
    @PostMapping("/review/delete")
    public String deleteReview(
            HttpSession session,
            @RequestParam("reviewId") Integer reviewId,
            @RequestParam(value = "redirectUri", defaultValue = "/restaurant/review") String redirectUri) {
        
        // 1. 安全檢查：驗證會員是否登入
        MemberVO member = getCurrentMember(session);
        if (member == null) {
            return "redirect:/login.html?redirect=/restaurant/review";
        }
        
        // 2. 執行刪除商業邏輯
        // 提示：您的 reviewService.deleteReview(reviewId) 內，
        // 應包含把該筆訂位的 reviewStatus 改回可評論狀態（例如改為 true），讓使用者能重新撰寫。
        reviewService.userDeleteReview(reviewId);
        
        // 3. 重定向回原本的訂位紀錄頁面
        return "redirect:" + redirectUri;
    }
    
    
    private MemberVO getCurrentMember(HttpSession session) {
        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");

        if (loginMember == null || loginMember.getMemberId() == null) {
            return null;
        }

        return memberService.getMemberById(loginMember.getMemberId());
    }

}
