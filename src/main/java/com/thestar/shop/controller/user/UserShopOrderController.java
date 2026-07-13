package com.thestar.shop.controller.user;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thestar.member.entity.MemberVO;
import com.thestar.shop.entity.CartItemVO;
import com.thestar.shop.entity.ProductOrderItemVO;
import com.thestar.shop.entity.ShopOrderVO;
import com.thestar.shop.service.CartItemService;
import com.thestar.shop.service.ProductOrderItemService;
import com.thestar.shop.service.ShopOrderService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/shop/order")
public class UserShopOrderController {

	@Autowired
	ShopOrderService shopOrderSvc;

	@Autowired
	ProductOrderItemService productOrderItemSvc;

	@Autowired
	CartItemService cartItemSvc;

	// 取得登入會員
	private MemberVO getLoginMember(HttpSession session) {
		return (MemberVO) session.getAttribute("loginMember");
	}

	// 顯示結帳頁面
	@GetMapping("checkout")
	public String checkout(HttpSession session, ModelMap model) {

		MemberVO loginMember = getLoginMember(session);
		if (loginMember == null) {
			return "redirect:/login.html";
		}

		List<CartItemVO> cartList = cartItemSvc.getByMemberId(loginMember.getMemberId());

		// 購物車是空的就導回購物車
		if (cartList == null || cartList.isEmpty()) {
			return "redirect:/shop/cart";
		}

		// 計算總計
		int total = 0;
		for (CartItemVO item : cartList) {
			if (item.getProduct() != null) {
				total += item.getProduct().getProductPrice() * item.getCartItemProdQty();
			}
		}

		model.addAttribute("cartListData", cartList);
		model.addAttribute("cartTotal", total);
		model.addAttribute("loginMember", loginMember);
		return "user/shop/order/checkout";
	}

	// 建立訂單（結帳送出）
	@PostMapping("placeOrder")
	public String placeOrder(
			@RequestParam("shopOrderName") String shopOrderName,
			@RequestParam("shopOrderPhone") String shopOrderPhone,
			@RequestParam(value = "shopOrderAddress", required = false) String shopOrderAddress,
			@RequestParam("shopOrderPickup") Byte shopOrderPickup,
			@RequestParam(value = "shopOrderPickupTime", required = false) String shopOrderPickupTime,
			@RequestParam(value = "shopOrderNote", required = false) String shopOrderNote,
			HttpSession session) {

		MemberVO loginMember = getLoginMember(session);
		if (loginMember == null) {
			return "redirect:/login.html";
		}

		List<CartItemVO> cartList = cartItemSvc.getByMemberId(loginMember.getMemberId());
		if (cartList == null || cartList.isEmpty()) {
			return "redirect:/shop/cart";
		}

		// 計算總金額
		int total = 0;
		for (CartItemVO item : cartList) {
			if (item.getProduct() != null) {
				total += item.getProduct().getProductPrice() * item.getCartItemProdQty();
			}
		}

		// 建立訂單
		ShopOrderVO order = new ShopOrderVO();
		order.setMemberId(loginMember.getMemberId());
		order.setShopOrderTime(LocalDateTime.now());
		order.setShopOrderTotal(total);
		order.setShopOrderName(shopOrderName);
		order.setShopOrderPhone(shopOrderPhone);
		order.setShopOrderPickup(shopOrderPickup);
		order.setShopOrderNote(shopOrderNote);
		order.setShopOrderStatus((byte) 0);   // 待處理
		order.setShopPaymentStatus((byte) 0); // 待付款

		// 自取 → 儲存取貨時間；宅配 → 儲存地址
		if (shopOrderPickup == 1 && shopOrderPickupTime != null && !shopOrderPickupTime.isEmpty()) {
			order.setShopOrderPickupTime(LocalDateTime.parse(shopOrderPickupTime + ":00"));
		} else {
			order.setShopOrderAddress(shopOrderAddress);
		}

		shopOrderSvc.addShopOrder(order);

		// 建立訂單明細
		for (CartItemVO item : cartList) {
			if (item.getProduct() == null) continue;

			ProductOrderItemVO orderItem = new ProductOrderItemVO();
			orderItem.setShopOrderId(order.getShopOrderId());
			orderItem.setProductId(item.getProductId());
			orderItem.setProdOrderItemQty(item.getCartItemProdQty());
			orderItem.setProdOrderItemPrice(item.getProduct().getProductPrice());
			orderItem.setProdOrderReviewStat((byte) 0); // 未評論
			productOrderItemSvc.addProductOrderItem(orderItem);
		}

		// 清空購物車
		for (CartItemVO item : cartList) {
			cartItemSvc.deleteCartItem(item.getCartItemId());
		}

		// 導向付款成功頁（帶訂單 ID）
		return "redirect:/shop/order/success?orderId=" + order.getShopOrderId();
	}

	// 付款成功頁面
	@GetMapping("success")
	public String orderSuccess(
			@RequestParam("orderId") Integer orderId,
			HttpSession session,
			ModelMap model) {

		MemberVO loginMember = getLoginMember(session);
		if (loginMember == null) {
			return "redirect:/login.html";
		}

		ShopOrderVO order = shopOrderSvc.getOneShopOrder(orderId);
		List<ProductOrderItemVO> itemList = productOrderItemSvc.getByShopOrderId(orderId);

		model.addAttribute("shopOrderVO", order);
		model.addAttribute("itemListData", itemList);
		return "user/shop/order/orderSuccess";
	}

	// 查看我的訂單
	@GetMapping("myOrders")
	public String myOrders(HttpSession session, ModelMap model) {

		MemberVO loginMember = getLoginMember(session);
		if (loginMember == null) {
			return "redirect:/login.html";
		}

		List<ShopOrderVO> orderList = shopOrderSvc.getByMemberId(loginMember.getMemberId());
		model.addAttribute("orderListData", orderList);
		return "user/shop/order/myOrders";
	}
}