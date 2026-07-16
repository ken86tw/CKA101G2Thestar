package com.thestar.shop.controller.user;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.thestar.member.entity.MemberVO;
import com.thestar.member.service.MemberNotifyService;
import com.thestar.shop.entity.CartItemVO;
import com.thestar.shop.entity.ProductOrderItemVO;
import com.thestar.shop.entity.ProductsVO;
import com.thestar.shop.entity.ShopOrderVO;
import com.thestar.shop.service.CartItemService;
import com.thestar.shop.service.ProductOrderItemService;
import com.thestar.shop.service.ProductsService;
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
	
	@Autowired
	ProductsService productsSvc;

	@Autowired
	MemberNotifyService memberNotifySvc;

	// ===== 綠界設定（從 application.properties 讀取）=====
	@Value("${ecpay.merchant-id}")
	private String MERCHANT_ID;

	@Value("${ecpay.hash-key}")
	private String HASH_KEY;

	@Value("${ecpay.hash-iv}")
	private String HASH_IV;

	@Value("${ecpay.aio-url}")
	private String ECPAY_URL;

//	@Value("${ecpay.return-url}")
//	private String BASE_URL;
	private String BASE_URL = "https://scoop-moonbeam-casing.ngrok-free.dev";

	// 取得登入會員
	private MemberVO getLoginMember(HttpSession session) {
		return (MemberVO) session.getAttribute("loginMember");
	}

	// 顯示結帳頁面
	@GetMapping("checkout")
	public String checkout(HttpSession session, ModelMap model) {
		MemberVO loginMember = getLoginMember(session);
		if (loginMember == null)
			return "redirect:/login.html";

		List<CartItemVO> cartList = cartItemSvc.getByMemberId(loginMember.getMemberId());
		if (cartList == null || cartList.isEmpty())
			return "redirect:/shop/cart";

		int total = 0;
		for (CartItemVO item : cartList) {
			if (item.getProduct() != null) {
				total += item.getProduct().getProductPrice() * item.getCartItemProdQty();
			}
		}

		// 檢查是否有超過庫存的商品
		boolean hasOverStock = cartList.stream()
				.anyMatch(item -> item.getProduct() != null
						&& item.getCartItemProdQty() > item.getProduct().getProductQuantity());

		model.addAttribute("cartListData", cartList);
		model.addAttribute("cartTotal", total);
		model.addAttribute("loginMember", loginMember);
		model.addAttribute("hasOverStock", hasOverStock);
		return "user/shop/order/checkout";
	}

	// 建立訂單並轉跳綠界付款
	@PostMapping("placeOrder")
	@ResponseBody
	public String placeOrder(@RequestParam("shopOrderName") String shopOrderName,
			@RequestParam("shopOrderPhone") String shopOrderPhone,
			@RequestParam(value = "shopOrderAddress", required = false) String shopOrderAddress,
			@RequestParam("shopOrderPickup") Byte shopOrderPickup,
			@RequestParam(value = "shopOrderPickupTime", required = false) String shopOrderPickupTime,
			@RequestParam(value = "shopOrderNote", required = false) String shopOrderNote, HttpSession session)
			throws Exception {

		MemberVO loginMember = getLoginMember(session);
		if (loginMember == null)
			return "<script>location.href='/login.html'</script>";

		List<CartItemVO> cartList = cartItemSvc.getByMemberId(loginMember.getMemberId());
		if (cartList == null || cartList.isEmpty())
			return "<script>location.href='/shop/cart'</script>";

		// 計算總金額
		int total = 0;
		for (CartItemVO item : cartList) {
			if (item.getProduct() != null)
				total += item.getProduct().getProductPrice() * item.getCartItemProdQty();
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
		order.setShopOrderStatus((byte) 0);
		order.setShopPaymentStatus((byte) 0); // 待付款

		if (shopOrderPickup == 1 && shopOrderPickupTime != null && !shopOrderPickupTime.isEmpty()) {
			order.setShopOrderPickupTime(LocalDateTime.parse(shopOrderPickupTime + ":00"));
		} else {
			order.setShopOrderAddress(shopOrderAddress);
		}

		shopOrderSvc.addShopOrder(order);

		// 發送訂單建立通知
		memberNotifySvc.createNotification(loginMember.getMemberId(),
				"您的購物訂單已成立，訂單編號：" + order.getShopOrderId() + "，請盡快完成付款！");

		// 建立訂單明細
		StringBuilder itemNames = new StringBuilder();
		for (CartItemVO item : cartList) {
			if (item.getProduct() == null)
				continue;
			ProductOrderItemVO orderItem = new ProductOrderItemVO();
			orderItem.setShopOrderId(order.getShopOrderId());
			orderItem.setProductId(item.getProductId());
			orderItem.setProdOrderItemQty(item.getCartItemProdQty());
			orderItem.setProdOrderItemPrice(item.getProduct().getProductPrice());
			orderItem.setProdOrderReviewStat((byte) 0);
			productOrderItemSvc.addProductOrderItem(orderItem);

			if (itemNames.length() > 0)
			    itemNames.append("#");
			itemNames.append(item.getProduct().getProductName());

			// 扣除庫存
			ProductsVO product = productsSvc.getOneProduct(item.getProductId());
			if (product != null) {
			    int newQty = product.getProductQuantity() - item.getCartItemProdQty();
			    product.setProductQuantity(Math.max(newQty, 0));
			    productsSvc.updateProduct(product);
			}
		}

		// 清空購物車
		for (CartItemVO item : cartList) {
			cartItemSvc.deleteCartItem(item.getCartItemId());
		}

		// ===== 產生綠界付款參數 =====
		String merchantTradeNo = "SHOP" + order.getShopOrderId() + System.currentTimeMillis() % 10000;
		if (merchantTradeNo.length() > 20)
			merchantTradeNo = merchantTradeNo.substring(0, 20);

		String tradeDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));

		Map<String, String> params = new LinkedHashMap<>();
		params.put("MerchantID", MERCHANT_ID);
		params.put("MerchantTradeNo", merchantTradeNo);
		params.put("MerchantTradeDate", tradeDate);
		params.put("PaymentType", "aio");
		params.put("TotalAmount", String.valueOf(total));
		params.put("TradeDesc", "東方之星購物結帳");
		params.put("ItemName", itemNames.toString());
		params.put("ReturnURL", BASE_URL + "/shop/order/ecpay/return");
		params.put("OrderResultURL", BASE_URL + "/shop/order/ecpay/result?orderId=" + order.getShopOrderId());
		params.put("ChoosePayment", "ALL");
		params.put("EncryptType", "1");
		params.put("CheckMacValue", generateCheckMacValue(params));

		// 產生自動送出的 HTML 表單
		StringBuilder html = new StringBuilder();
		html.append("<form id='ecpayForm' method='post' action='").append(ECPAY_URL).append("'>");
		for (Map.Entry<String, String> entry : params.entrySet()) {
			html.append("<input type='hidden' name='").append(entry.getKey()).append("' value='")
					.append(entry.getValue()).append("'>");
		}
		html.append("</form>");
		html.append("<script>document.getElementById('ecpayForm').submit();</script>");
		return html.toString();
	}

	// 綠界付款完成後回呼（ReturnURL）
	@PostMapping("ecpay/return")
	@ResponseBody
	public String ecpayReturn(@RequestParam Map<String, String> params) throws Exception {
		// 驗證 CheckMacValue
		String receivedMac = params.get("CheckMacValue");
		Map<String, String> paramsWithoutMac = new LinkedHashMap<>(params);
		paramsWithoutMac.remove("CheckMacValue");
		String calculatedMac = generateCheckMacValue(paramsWithoutMac);

		if (!receivedMac.equalsIgnoreCase(calculatedMac)) {
			return "0|ErrorMessage";
		}

		// 付款成功（RtnCode == 1）
		if ("1".equals(params.get("RtnCode"))) {
			String merchantTradeNo = params.get("MerchantTradeNo");
			try {
				String orderIdStr = merchantTradeNo.replace("SHOP", "");
				orderIdStr = orderIdStr.replaceAll("[^0-9]", "").substring(0,
						orderIdStr.replaceAll("[^0-9]", "").length() - 4);
				Integer orderId = Integer.parseInt(orderIdStr);
				ShopOrderVO order = shopOrderSvc.getOneShopOrder(orderId);
				if (order != null) {
					order.setShopPaymentStatus((byte) 1); // 已付款
					order.setShopOrderStatus((byte) 1); // 處理中
					shopOrderSvc.updateShopOrder(order);

					// 發送付款成功通知
					memberNotifySvc.createNotification(order.getMemberId(), "購物訂單編號 " + orderId + " 付款成功，感謝您的購買！");
				}
			} catch (Exception e) {
				// 解析失敗不影響回傳
			}
		}

		return "1|OK";
	}

	// 付款結果頁（OrderResultURL，給使用者看）
	@org.springframework.web.bind.annotation.RequestMapping(value = "ecpay/result", method = {
			org.springframework.web.bind.annotation.RequestMethod.GET,
			org.springframework.web.bind.annotation.RequestMethod.POST })
	public String ecpayResult(@RequestParam("orderId") Integer orderId, HttpSession session, ModelMap model) {

		ShopOrderVO order = shopOrderSvc.getOneShopOrder(orderId);
		List<ProductOrderItemVO> itemList = productOrderItemSvc.getByShopOrderId(orderId);

		model.addAttribute("shopOrderVO", order);
		model.addAttribute("itemListData", itemList);
		return "user/shop/order/orderSuccess";
	}

	// 重新付款（針對待付款訂單）
	@GetMapping("pay/{orderId}")
	@ResponseBody
	public String repay(@org.springframework.web.bind.annotation.PathVariable Integer orderId, HttpSession session)
			throws Exception {

		MemberVO loginMember = getLoginMember(session);
		if (loginMember == null)
			return "<script>location.href='/login.html'</script>";

		ShopOrderVO order = shopOrderSvc.getOneShopOrder(orderId);
		if (order == null || !order.getMemberId().equals(loginMember.getMemberId())) {
			return "<script>location.href='/shop/order/myOrders'</script>";
		}
		if (order.getShopPaymentStatus() != 0) {
			return "<script>location.href='/shop/order/myOrders'</script>";
		}

		List<ProductOrderItemVO> itemList = productOrderItemSvc.getByShopOrderId(orderId);
		StringBuilder itemNames = new StringBuilder();
		for (ProductOrderItemVO item : itemList) {
			if (itemNames.length() > 0)
				itemNames.append("#");
			itemNames.append(item.getProduct() != null ? item.getProduct().getProductName() : "商品");
		}

		String merchantTradeNo = "SHOP" + orderId + System.currentTimeMillis() % 10000;
		if (merchantTradeNo.length() > 20)
			merchantTradeNo = merchantTradeNo.substring(0, 20);
		String tradeDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));

		Map<String, String> params = new LinkedHashMap<>();
		params.put("MerchantID", MERCHANT_ID);
		params.put("MerchantTradeNo", merchantTradeNo);
		params.put("MerchantTradeDate", tradeDate);
		params.put("PaymentType", "aio");
		params.put("TotalAmount", String.valueOf(order.getShopOrderTotal()));
		params.put("TradeDesc", "東方之星購物結帳");
		params.put("ItemName", itemNames.length() > 0 ? itemNames.toString() : "商品");
		params.put("ReturnURL", BASE_URL + "/shop/order/ecpay/return");
		params.put("OrderResultURL", BASE_URL + "/shop/order/ecpay/result?orderId=" + orderId);
		params.put("ChoosePayment", "ALL");
		params.put("EncryptType", "1");
		params.put("CheckMacValue", generateCheckMacValue(params));

		StringBuilder html = new StringBuilder();
		html.append("<form id='ecpayForm' method='post' action='").append(ECPAY_URL).append("'>");
		for (Map.Entry<String, String> entry : params.entrySet()) {
			html.append("<input type='hidden' name='").append(entry.getKey()).append("' value='")
					.append(entry.getValue()).append("'>");
		}
		html.append("</form>");
		html.append("<script>document.getElementById('ecpayForm').submit();</script>");
		return html.toString();
	}

	// 查看我的訂單
	@GetMapping("myOrders")
	public String myOrders(HttpSession session, ModelMap model) {
		MemberVO loginMember = getLoginMember(session);
		if (loginMember == null)
			return "redirect:/login.html";

		List<ShopOrderVO> orderList = shopOrderSvc.getByMemberId(loginMember.getMemberId());

		Map<Integer, List<ProductOrderItemVO>> orderItemsMap = new java.util.HashMap<>();
		for (ShopOrderVO order : orderList) {
			orderItemsMap.put(order.getShopOrderId(),
					productOrderItemSvc.getByShopOrderId(order.getShopOrderId()));
		}

		model.addAttribute("orderListData", orderList);
		model.addAttribute("orderItemsMap", orderItemsMap);
		return "user/shop/order/myOrders";
	}

	// ===== 產生 CheckMacValue =====
	private String generateCheckMacValue(Map<String, String> params) throws Exception {
		String raw = params.entrySet().stream().sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
				.map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));

		raw = "HashKey=" + HASH_KEY + "&" + raw + "&HashIV=" + HASH_IV;
		raw = URLEncoder.encode(raw, "UTF-8").toLowerCase();
		raw = raw.replace("%2d", "-").replace("%5f", "_").replace("%2e", ".").replace("%21", "!").replace("%2a", "*")
				.replace("%28", "(").replace("%29", ")");

		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
		StringBuilder sb = new StringBuilder();
		for (byte b : hash)
			sb.append(String.format("%02X", b));
		return sb.toString();
	}
}