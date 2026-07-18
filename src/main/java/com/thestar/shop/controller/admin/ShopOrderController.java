package com.thestar.shop.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thestar.shop.entity.ProductOrderItemVO;
import com.thestar.shop.entity.ShopOrderVO;
import com.thestar.shop.service.ShopOrderService;
import com.thestar.shop.service.ProductOrderItemService;

@Controller
@RequestMapping("/admin/shop/order")
public class ShopOrderController extends AdminShopBaseController {

	@Autowired
	ShopOrderService shopOrderSvc;

	// 顯示所有訂單
	@GetMapping("listAllOrders")
	public String listAllOrders(
	        @RequestParam(value = "paymentStatus", required = false) Byte paymentStatus,
	        @RequestParam(value = "filter", required = false) String filter,
	        ModelMap model) {
	    
	    List<ShopOrderVO> list = shopOrderSvc.getAll();

	    // 付款狀態篩選（待付款）
	    if (paymentStatus != null) {
	        list = list.stream()
	                .filter(o -> o.getShopPaymentStatus() != null
	                          && o.getShopPaymentStatus().equals(paymentStatus)
	                          && o.getShopOrderStatus() != null
	                          && o.getShopOrderStatus() != 3)
	                .collect(java.util.stream.Collectors.toList());
	    }

	    // 訂單狀態篩選
	    if ("pending".equals(filter)) {
	        // 待出貨：已付款 + 訂單成立
	        list = list.stream()
	                .filter(o -> o.getShopPaymentStatus() != null && o.getShopPaymentStatus() == 1
	                          && o.getShopOrderStatus() != null && o.getShopOrderStatus() == 0)
	                .collect(java.util.stream.Collectors.toList());
	    } else if ("shipping".equals(filter)) {
	        // 出貨中
	        list = list.stream()
	                .filter(o -> o.getShopOrderStatus() != null && o.getShopOrderStatus() == 1)
	                .collect(java.util.stream.Collectors.toList());
	    } else if ("delivered".equals(filter)) {
	        // 已送達
	        list = list.stream()
	                .filter(o -> o.getShopOrderStatus() != null && o.getShopOrderStatus() == 2)
	                .collect(java.util.stream.Collectors.toList());
	    } else if ("cancelled".equals(filter)) {
	        // 已取消
	        list = list.stream()
	                .filter(o -> o.getShopOrderStatus() != null && o.getShopOrderStatus() == 3)
	                .collect(java.util.stream.Collectors.toList());
	    }

	    model.addAttribute("orderListData", list);
	    model.addAttribute("selectedPaymentStatus", paymentStatus);
	    model.addAttribute("selectedFilter", filter);
	    return "admin/shop/order/listAllOrders";
	}

	// 顯示單筆訂單
	@Autowired
	ProductOrderItemService productOrderItemSvc;

	@PostMapping("getOne")
	public String getOne(@RequestParam("shopOrderId") Integer shopOrderId, ModelMap model) {
		ShopOrderVO shopOrderVO = shopOrderSvc.getOneShopOrder(shopOrderId);
		List<ProductOrderItemVO> itemList = productOrderItemSvc.getByShopOrderId(shopOrderId);
		model.addAttribute("shopOrderVO", shopOrderVO);
		model.addAttribute("itemListData", itemList);
		return "admin/shop/order/listOneOrder";
	}

	// 更新訂單狀態
	@PostMapping("updateStatus")
	public String updateStatus(@RequestParam("shopOrderId") Integer shopOrderId,
			@RequestParam("shopOrderStatus") Byte shopOrderStatus,
			@RequestParam("shopPaymentStatus") Byte shopPaymentStatus) {
		ShopOrderVO shopOrderVO = shopOrderSvc.getOneShopOrder(shopOrderId);
		if (shopOrderVO != null) {
			shopOrderVO.setShopOrderStatus(shopOrderStatus);
			shopOrderVO.setShopPaymentStatus(shopPaymentStatus);
			shopOrderSvc.updateShopOrder(shopOrderVO);
		}
		return "redirect:/admin/shop/order/listAllOrders";
	}
}