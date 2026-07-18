package com.thestar.shop.controller.admin;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.thestar.shop.entity.ProductsVO;
import com.thestar.shop.entity.ShopOrderVO;
import com.thestar.shop.service.ProductReviewService;
import com.thestar.shop.service.ProductsService;
import com.thestar.shop.service.ShopOrderService;

@Controller
@RequestMapping("/admin/shop")
public class AdminShopIndexController extends AdminShopBaseController{

	@Autowired
	ProductsService productsSvc;

	@Autowired
	ShopOrderService shopOrderSvc;

	@Autowired
	ProductReviewService productReviewSvc;

	@GetMapping
	public String index(ModelMap model) {
		List<ProductsVO> allProducts = productsSvc.getAll();
		List<ShopOrderVO> allOrders = shopOrderSvc.getAll();

		// 商品統計
		long totalProducts = allProducts.size();
		long activeProducts = allProducts.stream()
				.filter(p -> p.getProductStatus() != null && p.getProductStatus() == 1)
				.count();

		// 庫存不足商品（數量 <= 5）
		List<ProductsVO> lowStockProducts = allProducts.stream()
				.filter(p -> p.getProductQuantity() != null && p.getProductQuantity() <= 5)
				.collect(Collectors.toList());

		// 訂單統計
		long totalOrders = allOrders.size();

		// 待處理：訂單成立且未取消
		long pendingOrders = allOrders.stream()
				.filter(o -> o.getShopOrderStatus() != null && o.getShopOrderStatus() == 0)
				.count();

		// 待付款：未付款且未取消
		long unpaidOrders = allOrders.stream()
				.filter(o -> o.getShopPaymentStatus() != null && o.getShopPaymentStatus() == 0
						&& o.getShopOrderStatus() != null && o.getShopOrderStatus() != 3)
				.count();

		// 待處理訂單：已付款但訂單狀態為訂單成立（尚未出貨）
		long processingOrders = allOrders.stream()
				.filter(o -> o.getShopPaymentStatus() != null && o.getShopPaymentStatus() == 1
						&& o.getShopOrderStatus() != null && o.getShopOrderStatus() == 0)
				.count();

		// 評論統計
		long totalReviews = productReviewSvc.getAll().size();

		model.addAttribute("totalProducts", totalProducts);
		model.addAttribute("activeProducts", activeProducts);
		model.addAttribute("lowStockProducts", lowStockProducts);
		model.addAttribute("totalOrders", totalOrders);
		model.addAttribute("pendingOrders", pendingOrders);
		model.addAttribute("unpaidOrders", unpaidOrders);
		model.addAttribute("processingOrders", processingOrders);
		model.addAttribute("totalReviews", totalReviews);
		return "admin/shop/index";
	}
}