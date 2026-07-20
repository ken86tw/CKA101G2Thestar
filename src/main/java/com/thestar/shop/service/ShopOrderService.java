package com.thestar.shop.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thestar.member.service.MemberNotifyService;
import com.thestar.shop.entity.ProductOrderItemVO;
import com.thestar.shop.entity.ProductsVO;
import com.thestar.shop.entity.ShopOrderVO;
import com.thestar.shop.repository.ShopOrderRepository;

@Service
public class ShopOrderService {

	private static final Logger log = LoggerFactory.getLogger(ShopOrderService.class);

	/** 配送方式：0 = 自取，1 = 宅配 */
	private static final byte PICKUP_SELF = 0;

	/** 訂單狀態：0 成立 / 1 出貨中 / 2 已送達 / 3 已取消 */
	private static final byte ORDER_CANCELLED = 3;

	/** 付款狀態：0 待付款 / 1 已付款 / 2 已退款 */
	private static final byte PAY_PAID = 1;

	@Autowired
	ShopOrderRepository repository;

	@Autowired
	MemberNotifyService memberNotifySvc;

	@Autowired
	ProductOrderItemService productOrderItemSvc;

	@Autowired
	ProductsService productsSvc;

	// ===================== 查詢 =====================

	public ShopOrderVO getOneShopOrder(Integer shopOrderId) {
		Optional<ShopOrderVO> optional = repository.findById(shopOrderId);
		return optional.orElse(null);
	}

	public List<ShopOrderVO> getAll() {
		return repository.findAll();
	}

	public List<ShopOrderVO> getByMemberId(Integer memberId) {
		return repository.findByMemberId(memberId);
	}

	// ===================== 新增 / 修改 / 刪除 =====================

	@Transactional
	public void addShopOrder(ShopOrderVO shopOrderVO) {
		repository.save(shopOrderVO);
		// 訂單建立通知
		memberNotifySvc.createNotification(shopOrderVO.getMemberId(),
				"您的購物訂單已成立，訂單編號：" + shopOrderVO.getShopOrderId() + "，請盡快完成付款！");
	}

	@Transactional
	public void updateShopOrder(ShopOrderVO shopOrderVO) {
		repository.save(shopOrderVO);
	}

	@Transactional
	public void deleteShopOrder(Integer shopOrderId) {
		if (repository.existsById(shopOrderId))
			repository.deleteByShopOrderId(shopOrderId);
	}

	// ===================== 付款 =====================

	/**
	 * 綠界付款成功後回寫。
	 * 綠界的 ReturnURL 在未收到 1|OK 前會重送，因此以資料庫目前狀態判斷是否已處理過，
	 * 避免使用者收到多則「付款成功」通知。
	 */
	@Transactional
	public void updateShopOrderWithPayment(ShopOrderVO shopOrderVO, Integer orderId) {
		Byte currentPayment = getPersistedPaymentStatus(orderId);
		Byte currentStatus = getPersistedOrderStatus(orderId);

		// 訂單已取消卻收到付款成功：不覆蓋狀態，避免出現「已取消 + 已付款」卻無人察覺。
		// 此時款項已實際收取，需人工退款處理。
		if (currentStatus != null && currentStatus == ORDER_CANCELLED) {
			log.warn("已取消的訂單收到付款成功回呼，需人工確認退款。orderId={}, memberId={}",
					orderId, shopOrderVO.getMemberId());
			memberNotifySvc.createNotification(shopOrderVO.getMemberId(),
					"購物訂單編號 " + orderId + " 已於付款前取消，款項將由客服為您辦理退款，造成不便敬請見諒。");
			return;
		}

		boolean alreadyPaid = currentPayment != null && currentPayment == PAY_PAID;

		repository.save(shopOrderVO);

		// 綠界的 ReturnURL 在未收到 1|OK 前會重送，已付款就不再重複通知
		if (!alreadyPaid) {
			memberNotifySvc.createNotification(shopOrderVO.getMemberId(),
					"購物訂單編號 " + orderId + " 付款成功，感謝您的購買！");
		}
	}

	// ===================== 取消（含庫存回補） =====================

	/**
	 * 逾時未付款自動取消。會將狀態設為 3 並回補庫存。
	 */
	@Transactional
	public void cancelShopOrder(ShopOrderVO shopOrderVO) {
		if (isAlreadyCancelled(shopOrderVO))
			return;

		shopOrderVO.setShopOrderStatus(ORDER_CANCELLED);
		repository.save(shopOrderVO);

		restoreStock(shopOrderVO.getShopOrderId());

		memberNotifySvc.createNotification(shopOrderVO.getMemberId(),
				"購物訂單編號 " + shopOrderVO.getShopOrderId() + " 因逾時未付款已自動取消。");
	}

	/**
	 * 客服手動取消。行為與 cancelShopOrder 一致：自行設定狀態 3 並回補庫存。
	 * （原本此方法不設狀態、靠呼叫端先設好，容易誤用，已統一。）
	 */
	@Transactional
	public void cancelShopOrderManually(ShopOrderVO shopOrderVO) {
		if (isAlreadyCancelled(shopOrderVO))
			return;

		shopOrderVO.setShopOrderStatus(ORDER_CANCELLED);
		repository.save(shopOrderVO);

		restoreStock(shopOrderVO.getShopOrderId());

		memberNotifySvc.createNotification(shopOrderVO.getMemberId(),
				"您的購物訂單編號 " + shopOrderVO.getShopOrderId() + " 已由客服取消，如有疑問請聯繫我們。");
	}

	// ===================== 出貨 / 送達 =====================

	@Transactional
	public void shipShopOrder(ShopOrderVO shopOrderVO) {
		repository.save(shopOrderVO);

		String msg = isPickup(shopOrderVO)
				? "您的購物訂單編號 " + shopOrderVO.getShopOrderId() + " 已備貨完成，請於預約時間至門市取貨。"
				: "您的購物訂單編號 " + shopOrderVO.getShopOrderId() + " 已出貨，請耐心等候！";
		memberNotifySvc.createNotification(shopOrderVO.getMemberId(), msg);
	}

	@Transactional
	public void deliverShopOrder(ShopOrderVO shopOrderVO) {
		repository.save(shopOrderVO);

		String msg = isPickup(shopOrderVO)
				? "您的購物訂單編號 " + shopOrderVO.getShopOrderId() + " 已完成取貨，感謝您的購買！如需評論請至我的訂單頁面。"
				: "您的購物訂單編號 " + shopOrderVO.getShopOrderId() + " 已送達，感謝您的購買！如需評論請至我的訂單頁面。";
		memberNotifySvc.createNotification(shopOrderVO.getMemberId(), msg);
	}

	// ===================== 私有方法 =====================

	private boolean isPickup(ShopOrderVO order) {
		return order.getShopOrderPickup() != null && order.getShopOrderPickup() == PICKUP_SELF;
	}

	/** 以資料庫目前狀態判斷是否已取消，避免重複回補庫存與重複發通知 */
	private boolean isAlreadyCancelled(ShopOrderVO order) {
		if (order == null || order.getShopOrderId() == null)
			return true;
		Byte persisted = getPersistedOrderStatus(order.getShopOrderId());
		return persisted != null && persisted == ORDER_CANCELLED;
	}

	private Byte getPersistedOrderStatus(Integer orderId) {
		return repository.findById(orderId).map(ShopOrderVO::getShopOrderStatus).orElse(null);
	}

	private Byte getPersistedPaymentStatus(Integer orderId) {
		return repository.findById(orderId).map(ShopOrderVO::getShopPaymentStatus).orElse(null);
	}

	/** 取消訂單時，把該筆訂單的商品數量加回庫存 */
	private void restoreStock(Integer shopOrderId) {
		List<ProductOrderItemVO> itemList = productOrderItemSvc.getByShopOrderId(shopOrderId);
		if (itemList == null)
			return;

		for (ProductOrderItemVO item : itemList) {
			ProductsVO product = productsSvc.getOneProduct(item.getProductId());
			if (product == null || product.getProductQuantity() == null)
				continue;
			product.setProductQuantity(product.getProductQuantity() + item.getProdOrderItemQty());
			productsSvc.updateProduct(product);
		}
	}
}