package com.thestar.shop.controller.user;

import com.thestar.shop.entity.CartItemVO;
import com.thestar.shop.service.CartItemService;
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

	// 顯示購物車
	@GetMapping
	public String listCart(ModelMap model) {
		List<CartItemVO> list = cartItemSvc.getAll();

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
	public String addToCart(@RequestParam("productId") Integer productId, @RequestParam("qty") Integer qty) {
		Integer memberId = 1; // 暫時寫死，之後接會員登入

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
	public String deleteFromCart(@RequestParam("cartItemId") Integer cartItemId) {
		cartItemSvc.deleteCartItem(cartItemId);
		return "redirect:/shop/cart";
	}

	// 更新數量
	@PostMapping("update")
	public String updateCart(@RequestParam("cartItemId") Integer cartItemId, @RequestParam("qty") Integer qty) {
		CartItemVO cartItemVO = cartItemSvc.getOneCartItem(cartItemId);
		if (cartItemVO != null) {
			cartItemVO.setCartItemProdQty(qty);
			cartItemSvc.updateCartItem(cartItemVO);
		}
		return "redirect:/shop/cart";
	}
}