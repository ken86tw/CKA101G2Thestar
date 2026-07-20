package com.thestar.shop.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.thestar.shop.entity.ShopOrderVO;
import com.thestar.shop.service.ShopOrderService;

@Component
public class ShopOrderScheduler {

	private static final Logger log = LoggerFactory.getLogger(ShopOrderScheduler.class);

	/** 訂單狀態：0 成立 / 1 出貨中 / 2 已送達 / 3 已取消 */
	private static final byte ORDER_CREATED = 0;

	/** 付款狀態：0 待付款 / 1 已付款 / 2 已退款 */
	private static final byte PAY_UNPAID = 0;

	@Autowired
	ShopOrderService shopOrderSvc;

	/**
	 * 逾時未付款的取消門檻（分鐘）。
	 * 注意：若綠界 ChoosePayment 開放 ATM / 超商代碼繳費，其繳費期限為數日，
	 * 20 分鐘會導致「使用者事後繳費、但訂單早已取消」的狀況。
	 * 請依實際開放的付款方式調整，或改為依付款方式分別設定門檻。
	 */
	@Value("${shop.order.unpaid-timeout-minutes:20}")
	private int unpaidTimeoutMinutes;

	// 每分鐘執行一次（啟動後延遲 1 分鐘再開始，避免與應用程式初始化競爭）
	@Scheduled(fixedRate = 60000, initialDelay = 60000)
	public void cancelUnpaidOrders() {
		LocalDateTime deadline = LocalDateTime.now().minusMinutes(unpaidTimeoutMinutes);

		// TODO 效能：訂單量成長後改用 Repository 查詢直接撈符合條件的訂單，
		//      例如 findByShopPaymentStatusAndShopOrderStatusAndShopOrderTimeBefore(0, 0, deadline)
		List<ShopOrderVO> allOrders = shopOrderSvc.getAll();
		if (allOrders == null || allOrders.isEmpty())
			return;

		int cancelled = 0;

		for (ShopOrderVO order : allOrders) {
			if (!isExpiredUnpaid(order, deadline))
				continue;

			try {
				// 交由 Service 處理：設定狀態、回補庫存、發送通知，且具備冪等檢查。
				// 注意：庫存回補已在 cancelShopOrder 內完成，此處不可再自行回補，否則會重複加回。
				shopOrderSvc.cancelShopOrder(order);
				cancelled++;
			} catch (Exception e) {
				// 單筆失敗不中斷整批
				log.error("逾時訂單自動取消失敗，orderId={}", order.getShopOrderId(), e);
			}
		}

		if (cancelled > 0) {
			log.info("逾時未付款訂單自動取消完成，共 {} 筆", cancelled);
		}
	}

	/** 是否為「已成立、未付款、且超過門檻時間」的訂單 */
	private boolean isExpiredUnpaid(ShopOrderVO order, LocalDateTime deadline) {
		if (order == null)
			return false;

		Byte paymentStatus = order.getShopPaymentStatus();
		Byte orderStatus = order.getShopOrderStatus();

		return paymentStatus != null && paymentStatus == PAY_UNPAID
				&& orderStatus != null && orderStatus == ORDER_CREATED
				&& order.getShopOrderTime() != null
				&& order.getShopOrderTime().isBefore(deadline);
	}
}