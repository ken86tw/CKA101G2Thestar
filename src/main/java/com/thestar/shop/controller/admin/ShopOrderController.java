package com.thestar.shop.controller.admin;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thestar.shop.entity.ProductOrderItemVO;
import com.thestar.shop.entity.ShopOrderVO;
import com.thestar.shop.service.ProductOrderItemService;
import com.thestar.shop.service.ShopOrderService;

@Controller
@RequestMapping("/admin/shop/order")
public class ShopOrderController extends AdminShopBaseController {

	/** 訂單狀態：0 成立 / 1 出貨中 / 2 已送達 / 3 已取消 */
	private static final byte ORDER_CREATED = 0;
	private static final byte ORDER_SHIPPING = 1;
	private static final byte ORDER_DELIVERED = 2;
	private static final byte ORDER_CANCELLED = 3;

	/** 付款狀態：0 待付款 / 1 已付款 / 2 已退款 */
	private static final byte PAY_UNPAID = 0;
	private static final byte PAY_PAID = 1;
	private static final byte PAY_REFUNDED = 2;

	@Autowired
	ShopOrderService shopOrderSvc;

	@Autowired
	ProductOrderItemService productOrderItemSvc;

	// 顯示所有訂單
	@GetMapping("listAllOrders")
	public String listAllOrders(
			@RequestParam(value = "paymentStatus", required = false) Byte paymentStatus,
			@RequestParam(value = "filter", required = false) String filter,
			ModelMap model) {

		List<ShopOrderVO> list = shopOrderSvc.getAll();

		// 兩組篩選互斥：同時給值必定回傳空清單，因此以 filter 優先
		if (filter != null && !filter.isBlank()) {
			paymentStatus = null;
		}

		Predicate<ShopOrderVO> predicate = o -> true;

		if (paymentStatus != null) {
			final Byte target = paymentStatus;
			// 注意：此處刻意排除已取消的訂單（例如「待付款」分頁不應出現已取消單）
			predicate = o -> target.equals(o.getShopPaymentStatus())
					&& o.getShopOrderStatus() != null
					&& o.getShopOrderStatus() != ORDER_CANCELLED;

		} else if ("pending".equals(filter)) {
			// 待出貨：已付款 + 訂單成立
			predicate = o -> o.getShopPaymentStatus() != null && o.getShopPaymentStatus() == PAY_PAID
					&& o.getShopOrderStatus() != null && o.getShopOrderStatus() == ORDER_CREATED;

		} else if ("shipping".equals(filter)) {
			predicate = o -> o.getShopOrderStatus() != null && o.getShopOrderStatus() == ORDER_SHIPPING;

		} else if ("delivered".equals(filter)) {
			predicate = o -> o.getShopOrderStatus() != null && o.getShopOrderStatus() == ORDER_DELIVERED;

		} else if ("cancelled".equals(filter)) {
			predicate = o -> o.getShopOrderStatus() != null && o.getShopOrderStatus() == ORDER_CANCELLED;
		}

		list = list.stream().filter(predicate).collect(Collectors.toList());

		model.addAttribute("orderListData", list);
		model.addAttribute("selectedPaymentStatus", paymentStatus);
		model.addAttribute("selectedFilter", filter);
		return "admin/shop/order/listAllOrders";
	}

	// 顯示單筆訂單
	@PostMapping("getOne")
	public String getOne(@RequestParam("shopOrderId") Integer shopOrderId, ModelMap model) {
		ShopOrderVO shopOrderVO = shopOrderSvc.getOneShopOrder(shopOrderId);
		if (shopOrderVO == null) {
			return "redirect:/admin/shop/order/listAllOrders";
		}
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

		ShopOrderVO order = shopOrderSvc.getOneShopOrder(shopOrderId);

		// 找不到訂單就直接返回，避免後續帶 null 進 service
		if (order == null) {
			return "redirect:/admin/shop/order/listAllOrders";
		}

		// 值域檢查：避免撞上 DB 的 CHECK constraint 直接噴 500
		if (shopOrderStatus == null || shopOrderStatus < ORDER_CREATED || shopOrderStatus > ORDER_CANCELLED
				|| shopPaymentStatus == null || shopPaymentStatus < PAY_UNPAID || shopPaymentStatus > PAY_REFUNDED) {
			return "redirect:/admin/shop/order/listAllOrders";
		}

		// 先記下原狀態，用來判斷是否真的發生轉換
		Byte oldStatus = order.getShopOrderStatus();
		boolean statusChanged = !shopOrderStatus.equals(oldStatus);

		order.setShopOrderStatus(shopOrderStatus);
		order.setShopPaymentStatus(shopPaymentStatus);

		// 狀態沒變 → 只單純存檔，不重跑會產生副作用的流程
		// （cancelShopOrderManually 會回補庫存，重複執行會讓庫存虛增）
		if (!statusChanged) {
			shopOrderSvc.updateShopOrder(order);
			return "redirect:/admin/shop/order/listAllOrders";
		}

		switch (shopOrderStatus) {
		case ORDER_CANCELLED:
			shopOrderSvc.cancelShopOrderManually(order);
			break;
		case ORDER_DELIVERED:
			shopOrderSvc.deliverShopOrder(order);
			break;
		case ORDER_SHIPPING:
			shopOrderSvc.shipShopOrder(order);
			break;
		default:
			shopOrderSvc.updateShopOrder(order);
			break;
		}

		return "redirect:/admin/shop/order/listAllOrders";
	}
}