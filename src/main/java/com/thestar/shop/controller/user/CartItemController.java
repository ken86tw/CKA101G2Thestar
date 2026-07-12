package com.thestar.shop.controller.user;

import com.thestar.member.entity.MemberVO;
import com.thestar.shop.entity.CartItemVO;
import com.thestar.shop.service.CartItemService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/shop/cart")
public class CartItemController {

	@Autowired
	CartItemService cartItemSvc;

	// ===== 取得登入會員 ID 的共用方法 =====
	private Integer getLoginMemberId(HttpSession session) {
		MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
		if (loginMember == null) {
			return null;
		}
		return loginMember.getMemberId();
	}

	// 顯示購物車
	@GetMapping
	public String listCart(HttpSession session, ModelMap model) {

		Integer memberId = getLoginMemberId(session);
		if (memberId == null) {
			return "redirect:/login.html";
		}

		List<CartItemVO> list = cartItemSvc.getByMemberId(memberId);

		// 計算總計
		int total = 0;
		for (CartItemVO item : list) {
			if (item.getProduct() != null) {
				total += item.getProduct().getProductPrice() * item.getCartItemProdQty();
			}
		}

		model.addAttribute("cartListData", list);
		model.addAttribute("cartTotal", total);
		return "user/shop/cart/listCart";
	}

	// 加入購物車
	@PostMapping("add")
	public String addToCart(@RequestParam("productId") Integer productId,
			@RequestParam("qty") Integer qty,
			HttpSession session) {

		Integer memberId = getLoginMemberId(session);
		if (memberId == null) {
			return "redirect:/login.html";
		}

		// 檢查購物車是否已有此商品
		CartItemVO existing = cartItemSvc.getByMemberIdAndProductId(memberId, productId);
		if (existing != null) {
			// 已存在 → 增加數量
			existing.setCartItemProdQty(existing.getCartItemProdQty() + qty);
			cartItemSvc.updateCartItem(existing);
		} else {
			// 不存在 → 新增
			CartItemVO cartItemVO = new CartItemVO();
			cartItemVO.setProductId(productId);
			cartItemVO.setCartItemProdQty(qty);
			cartItemVO.setMemberId(memberId);
			cartItemSvc.addCartItem(cartItemVO);
		}

		return "redirect:/shop/cart";
	}

	// 刪除購物車項目
	@PostMapping("delete")
	public String deleteFromCart(@RequestParam("cartItemId") Integer cartItemId,
			HttpSession session) {

		Integer memberId = getLoginMemberId(session);
		if (memberId == null) {
			return "redirect:/login.html";
		}

		// 確認這個購物車項目屬於此會員，防止別人刪別人的東西
		CartItemVO item = cartItemSvc.getOneCartItem(cartItemId);
		if (item != null && item.getMemberId().equals(memberId)) {
			cartItemSvc.deleteCartItem(cartItemId);
		}

		return "redirect:/shop/cart";
	}

	// 更新數量
	@PostMapping("update")
	public String updateCart(@RequestParam("cartItemId") Integer cartItemId,
			@RequestParam("qty") Integer qty,
			HttpSession session) {

		Integer memberId = getLoginMemberId(session);
		if (memberId == null) {
			return "redirect:/login.html";
		}

		// 確認這個購物車項目屬於此會員
		CartItemVO cartItemVO = cartItemSvc.getOneCartItem(cartItemId);
		if (cartItemVO != null && cartItemVO.getMemberId().equals(memberId)) {
			cartItemVO.setCartItemProdQty(qty);
			cartItemSvc.updateCartItem(cartItemVO);
		}

		return "redirect:/shop/cart";
	}
}