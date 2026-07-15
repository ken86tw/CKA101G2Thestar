package com.thestar.shop.controller.user;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thestar.member.entity.MemberVO;
import com.thestar.member.service.MemberNotifyService;
import com.thestar.shop.entity.ProductOrderItemVO;
import com.thestar.shop.entity.ProductReviewVO;
import com.thestar.shop.entity.ProductsVO;
import com.thestar.shop.service.ProductOrderItemService;
import com.thestar.shop.service.ProductReviewService;
import com.thestar.shop.service.ProductsService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/shop/review")
public class UserProductReviewController {

	@Autowired
	ProductReviewService productReviewSvc;
	@Autowired
	ProductOrderItemService productOrderItemSvc;
	@Autowired
	ProductsService productsSvc;
	@Autowired
	MemberNotifyService memberNotifySvc;

	// 顯示評論表單
	@GetMapping("add")
	public String addReviewForm(@RequestParam("productOrderItemId") Integer productOrderItemId, HttpSession session,
			ModelMap model) {

		MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
		if (loginMember == null)
			return "redirect:/login.html";

		// 確認訂單項目存在且屬於此會員
		ProductOrderItemVO orderItem = productOrderItemSvc.getOneProductOrderItem(productOrderItemId);
		if (orderItem == null)
			return "redirect:/shop/order/myOrders";
		if (!orderItem.getShopOrder().getMemberId().equals(loginMember.getMemberId())) {
			return "redirect:/shop/order/myOrders";
		}

		// 確認訂單已完成才能評論
		if (orderItem.getShopOrder().getShopOrderStatus() != 2) {
			return "redirect:/shop/order/myOrders";
		}

		// 已評論過就導回
		if (productReviewSvc.existsByProductOrderItemId(productOrderItemId)) {
			return "redirect:/shop/order/myOrders";
		}

		model.addAttribute("orderItem", orderItem);
		return "user/shop/review/addReview";
	}

	// 送出評論
	@PostMapping("submit")
	public String submitReview(@RequestParam("productOrderItemId") Integer productOrderItemId,
			@RequestParam("productRate") Byte productRate,
			@RequestParam(value = "productReview", required = false) String productReview, HttpSession session) {

		MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
		if (loginMember == null)
			return "redirect:/login.html";

		ProductOrderItemVO orderItem = productOrderItemSvc.getOneProductOrderItem(productOrderItemId);
		if (orderItem == null)
			return "redirect:/shop/order/myOrders";
		if (!orderItem.getShopOrder().getMemberId().equals(loginMember.getMemberId())) {
			return "redirect:/shop/order/myOrders";
		}
		
		// 確認訂單已完成才能評論
		if (orderItem.getShopOrder().getShopOrderStatus() != 2) {
		    return "redirect:/shop/order/myOrders";
		}

		// 防止重複評論
		if (productReviewSvc.existsByProductOrderItemId(productOrderItemId)) {
			return "redirect:/shop/order/myOrders";
		}

		// 建立評論
		ProductReviewVO review = new ProductReviewVO();
		review.setProductId(orderItem.getProductId());
		review.setMemberId(loginMember.getMemberId());
		review.setProductOrderItemId(productOrderItemId);
		review.setProductRate(productRate);
		review.setProductReview(productReview);
		review.setProductReviewDate(LocalDateTime.now());
		productReviewSvc.addProductReview(review);

		// 更新商品平均星數
		ProductsVO product = productsSvc.getOneProduct(orderItem.getProductId());
		if (product != null) {
			productReviewSvc.updateAverageRating(product, productRate);
			productsSvc.updateProduct(product);
		}

		// 更新訂單項目評論狀態
		orderItem.setProdOrderReviewStat((byte) 1);
		productOrderItemSvc.updateProductOrderItem(orderItem);

		// 發送通知
		memberNotifySvc.createNotification(loginMember.getMemberId(),
				"您對「" + (orderItem.getProduct() != null ? orderItem.getProduct().getProductName() : "商品")
						+ "」的評論已送出，感謝您的回饋！");

		return "redirect:/shop/order/myOrders";
	}
}