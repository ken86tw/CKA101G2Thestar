package com.thestar.shop.controller.user;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

	/** 配送方式：0 = 自取(PICKUP)，1 = 宅配(DELIVERY)。對應 SHOP_ORDER.SHOP_ORDER_PICKUP */
	private static final byte PICKUP_SELF = 0;
	private static final byte PICKUP_DELIVERY = 1;

	/** 自取營業時段 */
	private static final LocalTime PICKUP_OPEN = LocalTime.of(9, 0);
	private static final LocalTime PICKUP_CLOSE = LocalTime.of(18, 0);

	@Autowired
	ShopOrderService shopOrderSvc;

	@Autowired
	ProductOrderItemService productOrderItemSvc;

	@Autowired
	CartItemService cartItemSvc;

	@Autowired
	ProductsService productsSvc;

	// ===== 綠界設定（從 application.properties 讀取）=====
	@Value("${ecpay.merchant-id}")
	private String MERCHANT_ID;

	@Value("${ecpay.hash-key}")
	private String HASH_KEY;

	@Value("${ecpay.hash-iv}")
	private String HASH_IV;

	@Value("${ecpay.aio-url}")
	private String ECPAY_URL;

	// ① 從 application.properties 讀 ecpay.base-url（只有網域，路徑在下面組字串時才接上）
	@Value("${ecpay.base-url}")
	private String BASE_URL;

	// 取得登入會員
	private MemberVO getLoginMember(HttpSession session) {
		return (MemberVO) session.getAttribute("loginMember");
	}

	// ② 判斷回傳網域：優先使用前端傳入的 clientBackUrl，否則 fallback 到 BASE_URL
	private String resolveResultBaseUrl(String clientBackUrl) {
		return (clientBackUrl != null && !clientBackUrl.isBlank()) ? clientBackUrl : BASE_URL;
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

		int total = calcTotal(cartList);

		model.addAttribute("cartListData", cartList);
		model.addAttribute("cartTotal", total);
		model.addAttribute("loginMember", loginMember);
		model.addAttribute("hasOverStock", hasOverStock(cartList));
		model.addAttribute("hasOffShelf", hasOffShelf(cartList));
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
			@RequestParam(value = "shopOrderNote", required = false) String shopOrderNote,
			// ③ 新增 clientBackUrl，讓前端可傳入自訂 OrderResultURL 的網域
			@RequestParam(value = "clientBackUrl", required = false) String clientBackUrl,
			HttpSession session)
			throws Exception {

		MemberVO loginMember = getLoginMember(session);
		if (loginMember == null)
			return "<script>location.href='/login.html'</script>";

		List<CartItemVO> cartList = cartItemSvc.getByMemberId(loginMember.getMemberId());
		if (cartList == null || cartList.isEmpty())
			return "<script>location.href='/shop/cart'</script>";

		// ===== 後端驗證（前端驗證可被繞過，這裡必須再驗一次）=====

		if (hasOffShelf(cartList))
			return alertAndBack("購物車中有已下架商品，請移除後再結帳！", "/shop/cart");

		if (shopOrderName == null || shopOrderName.isBlank())
			return alertAndBack("請輸入收件人姓名！", "/shop/order/checkout");

		if (shopOrderPhone == null || shopOrderPhone.isBlank())
			return alertAndBack("請輸入聯絡電話！", "/shop/order/checkout");

		if (shopOrderPickup == null
				|| (shopOrderPickup != PICKUP_SELF && shopOrderPickup != PICKUP_DELIVERY))
			return alertAndBack("配送方式有誤，請重新選擇！", "/shop/order/checkout");

		boolean overStock = hasOverStock(cartList);

		LocalDateTime pickupTime = null;

		if (shopOrderPickup == PICKUP_SELF) {
			// 自取：不可超庫存、必須有合法取貨時間
			if (overStock)
				return alertAndBack("購買數量超過庫存，無法選擇自取！", "/shop/order/checkout");

			if (shopOrderPickupTime == null || shopOrderPickupTime.isBlank())
				return alertAndBack("請選擇取貨時間！", "/shop/order/checkout");

			try {
				pickupTime = LocalDateTime.parse(shopOrderPickupTime.length() == 16
						? shopOrderPickupTime + ":00"
						: shopOrderPickupTime);
			} catch (Exception e) {
				return alertAndBack("取貨時間格式有誤！", "/shop/order/checkout");
			}

			if (pickupTime.toLocalDate().isBefore(LocalDateTime.now().toLocalDate().plusDays(1)))
				return alertAndBack("取貨時間最早為隔日！", "/shop/order/checkout");

			LocalTime t = pickupTime.toLocalTime();
			if (t.isBefore(PICKUP_OPEN) || !t.isBefore(PICKUP_CLOSE))
				return alertAndBack("取貨時間必須在 09:00 ~ 18:00 之間！", "/shop/order/checkout");

		} else {
			// 宅配：地址必填
			if (shopOrderAddress == null || shopOrderAddress.isBlank())
				return alertAndBack("請輸入收件地址！", "/shop/order/checkout");
		}

		// 計算總金額
		int total = calcTotal(cartList);

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

		// 0 = 自取 → 存取貨時間；1 = 宅配 → 存地址
		if (shopOrderPickup == PICKUP_SELF) {
			order.setShopOrderPickupTime(pickupTime);
		} else {
			order.setShopOrderAddress(shopOrderAddress);
		}

		shopOrderSvc.addShopOrder(order);

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
		return buildEcpayForm(order.getShopOrderId(), total, itemNames.toString(), clientBackUrl);
	}

	// 綠界付款完成後回呼（ReturnURL）
	@PostMapping("ecpay/return")
	@ResponseBody
	public String ecpayReturn(@RequestParam Map<String, String> params) throws Exception {
		// 驗證 CheckMacValue
		String receivedMac = params.get("CheckMacValue");
		if (receivedMac == null || receivedMac.isBlank())
			return "0|ErrorMessage";

		Map<String, String> paramsWithoutMac = new LinkedHashMap<>(params);
		paramsWithoutMac.remove("CheckMacValue");
		String calculatedMac = generateCheckMacValue(paramsWithoutMac);

		if (!receivedMac.equalsIgnoreCase(calculatedMac)) {
			return "0|ErrorMessage";
		}

		// 付款成功（RtnCode == 1）
		if ("1".equals(params.get("RtnCode"))) {
			Integer orderId = extractOrderId(params);
			if (orderId != null) {
				ShopOrderVO order = shopOrderSvc.getOneShopOrder(orderId);
				if (order != null) {
					order.setShopPaymentStatus((byte) 1); // 已付款
					shopOrderSvc.updateShopOrderWithPayment(order, orderId);
				}
			}
		}

		return "1|OK";
	}

	// 付款結果頁（OrderResultURL，給使用者看）
	// 注意：綠界是跨站 POST 回來，可能不會帶 Session，所以這裡不依賴登入 Session。
	@RequestMapping(value = "ecpay/result", method = {
	        org.springframework.web.bind.annotation.RequestMethod.GET,
	        org.springframework.web.bind.annotation.RequestMethod.POST })
	public String ecpayResult(@RequestParam("orderId") Integer orderId,
	                          ModelMap model) {

	    // 查詢訂單
	    ShopOrderVO order = shopOrderSvc.getOneShopOrder(orderId);

	    // 找不到訂單
	    if (order == null) {
	        return "redirect:/";
	    }

	    // 尚未付款，不允許顯示成功頁
	    if (order.getShopPaymentStatus() == null || order.getShopPaymentStatus() != 1) {
	        return "redirect:/shop/order/myOrders";
	    }

	    // 查詢訂單明細
	    List<ProductOrderItemVO> itemList =
	            productOrderItemSvc.getByShopOrderId(orderId);

	    model.addAttribute("shopOrderVO", order);
	    model.addAttribute("itemListData", itemList);

	    return "user/shop/order/orderSuccess";
	}

	// 重新付款（針對待付款訂單）
	@GetMapping("pay/{orderId}")
	@ResponseBody
	public String repay(@org.springframework.web.bind.annotation.PathVariable Integer orderId,
			// ⑤ 新增 clientBackUrl，讓前端可傳入自訂 OrderResultURL 的網域
			@RequestParam(value = "clientBackUrl", required = false) String clientBackUrl,
			HttpSession session)
			throws Exception {

		MemberVO loginMember = getLoginMember(session);
		if (loginMember == null)
			return "<script>location.href='/login.html'</script>";

		ShopOrderVO order = shopOrderSvc.getOneShopOrder(orderId);
		if (order == null || !order.getMemberId().equals(loginMember.getMemberId())) {
			return "<script>location.href='/shop/order/myOrders'</script>";
		}
		if (order.getShopPaymentStatus() == null || order.getShopPaymentStatus() != 0) {
			return "<script>location.href='/shop/order/myOrders'</script>";
		}

		List<ProductOrderItemVO> itemList = productOrderItemSvc.getByShopOrderId(orderId);
		StringBuilder itemNames = new StringBuilder();
		for (ProductOrderItemVO item : itemList) {
			if (itemNames.length() > 0)
				itemNames.append("#");
			itemNames.append(item.getProduct() != null ? item.getProduct().getProductName() : "商品");
		}

		return buildEcpayForm(orderId, order.getShopOrderTotal(), itemNames.toString(), clientBackUrl);
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

	// ===================== 共用私有方法 =====================

	private int calcTotal(List<CartItemVO> cartList) {
		int total = 0;
		for (CartItemVO item : cartList) {
			if (item.getProduct() != null)
				total += item.getProduct().getProductPrice() * item.getCartItemProdQty();
		}
		return total;
	}

	/** 是否有商品數量超過庫存 */
	private boolean hasOverStock(List<CartItemVO> cartList) {
		return cartList.stream().anyMatch(item -> item.getProduct() != null
				&& item.getCartItemProdQty() > item.getProduct().getProductQuantity());
	}

	/** 是否有已下架（或已被刪除）的商品 */
	private boolean hasOffShelf(List<CartItemVO> cartList) {
		return cartList.stream().anyMatch(item -> item.getProduct() == null
				|| item.getProduct().getProductStatus() == null
				|| item.getProduct().getProductStatus() != 1);
	}

	private String alertAndBack(String message, String url) {
		return "<script>alert('" + jsEscape(message) + "');location.href='" + url + "'</script>";
	}

	/** 組出自動送出的綠界表單 */
	private String buildEcpayForm(Integer orderId, int total, String itemNames, String clientBackUrl)
			throws Exception {

		// %04d：固定 4 位數，讓回呼端可穩定還原 orderId（舊版位數不固定會解析失敗）
		String merchantTradeNo = "SHOP" + orderId + String.format("%04d", System.currentTimeMillis() % 10000);
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
		params.put("ItemName", (itemNames == null || itemNames.isBlank()) ? "商品" : itemNames);
		params.put("ReturnURL", BASE_URL + "/shop/order/ecpay/return");
		// ④ OrderResultURL 使用 resolveResultBaseUrl 決定網域
		params.put("OrderResultURL",
				resolveResultBaseUrl(clientBackUrl) + "/shop/order/ecpay/result?orderId=" + orderId);
		params.put("ChoosePayment", "Credit");
		params.put("EncryptType", "1");
		// 直接帶訂單編號，回呼時不必從 MerchantTradeNo 反推
		params.put("CustomField1", String.valueOf(orderId));
		params.put("CheckMacValue", generateCheckMacValue(params));

		StringBuilder html = new StringBuilder();
		html.append("<form id='ecpayForm' method='post' action='").append(ECPAY_URL).append("'>");
		for (Map.Entry<String, String> entry : params.entrySet()) {
			html.append("<input type='hidden' name='").append(htmlEscape(entry.getKey())).append("' value='")
					.append(htmlEscape(entry.getValue())).append("'>");
		}
		html.append("</form>");
		html.append("<script>document.getElementById('ecpayForm').submit();</script>");
		return html.toString();
	}

	/**
	 * 從綠界回呼取出訂單編號。 優先讀 CustomField1；失敗則沿用舊格式 SHOPxxxx#### 反推（相容舊訂單）。
	 */
	private Integer extractOrderId(Map<String, String> params) {
		String custom = params.get("CustomField1");
		if (custom != null && custom.matches("\\d+")) {
			return Integer.valueOf(custom);
		}

		String merchantTradeNo = params.get("MerchantTradeNo");
		if (merchantTradeNo == null)
			return null;
		String digits = merchantTradeNo.replace("SHOP", "").replaceAll("[^0-9]", "");
		if (digits.length() <= 4)
			return null;
		try {
			return Integer.valueOf(digits.substring(0, digits.length() - 4));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private String htmlEscape(String s) {
		if (s == null)
			return "";
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\"", "&quot;").replace("'", "&#39;");
	}

	private String jsEscape(String s) {
		if (s == null)
			return "";
		return s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", " ");
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