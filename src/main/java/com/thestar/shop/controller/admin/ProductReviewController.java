package com.thestar.shop.controller.admin;

import com.thestar.shop.entity.ProductReviewVO;
import com.thestar.shop.service.ProductReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/shop/review")
public class ProductReviewController extends AdminShopBaseController {

    @Autowired
    ProductReviewService productReviewSvc;

    @GetMapping("listAllReviews")
    public String listAllReviews(ModelMap model) {
        List<ProductReviewVO> list = productReviewSvc.getAll();
        model.addAttribute("reviewListData", list);
        return "admin/shop/review/listAllReviews";
    }

    @PostMapping("delete")
    public String delete(@RequestParam("productReviewId") Integer productReviewId) {
        productReviewSvc.deleteProductReview(productReviewId);
        return "redirect:/admin/shop/review/listAllReviews";
    }
}