package com.thestar.restaurant.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.thestar.restaurant.entity.RestaurantReviewVO;
import com.thestar.restaurant.service.RestaurantReviewService;
import java.util.List;

@Controller
@RequestMapping("/admin/restaurant/review")
public class RestaurantReviewController {

    @Autowired
    private RestaurantReviewService reviewService;

    // 評論列表
    @GetMapping("/list")
    public String list(Model model) {
        List<RestaurantReviewVO> list = reviewService.getAll();
        model.addAttribute("reviewList", list);
        return "admin/restaurant/review/list";
    }

    // 刪除評論
    @PostMapping("/delete")
    public String delete(@RequestParam("reviewId") Integer reviewId) {
        reviewService.deleteReview(reviewId);
        return "redirect:/admin/restaurant/review/list";
    }
}