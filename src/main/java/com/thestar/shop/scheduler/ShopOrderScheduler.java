package com.thestar.shop.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.thestar.member.service.MemberNotifyService;
import com.thestar.shop.entity.ProductOrderItemVO;
import com.thestar.shop.entity.ProductsVO;
import com.thestar.shop.entity.ShopOrderVO;
import com.thestar.shop.service.ProductOrderItemService;
import com.thestar.shop.service.ProductsService;
import com.thestar.shop.service.ShopOrderService;

@Component
public class ShopOrderScheduler {

    @Autowired
    ShopOrderService shopOrderSvc;

    @Autowired
    ProductOrderItemService productOrderItemSvc;

    @Autowired
    ProductsService productsSvc;

    @Autowired
    MemberNotifyService memberNotifySvc;

    // 每分鐘執行一次
    @Scheduled(fixedRate = 60000)
    public void cancelUnpaidOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(20);

        List<ShopOrderVO> allOrders = shopOrderSvc.getAll();
        for (ShopOrderVO order : allOrders) {
            if (order.getShopPaymentStatus() == 0
                    && order.getShopOrderStatus() == 0
                    && order.getShopOrderTime() != null
                    && order.getShopOrderTime().isBefore(deadline)) {

                // 取消訂單
                order.setShopOrderStatus((byte) 3);
                shopOrderSvc.updateShopOrder(order);

                // 釋放庫存
                List<ProductOrderItemVO> itemList =
                    productOrderItemSvc.getByShopOrderId(order.getShopOrderId());
                for (ProductOrderItemVO item : itemList) {
                    ProductsVO product = productsSvc.getOneProduct(item.getProductId());
                    if (product != null) {
                        product.setProductQuantity(
                            product.getProductQuantity() + item.getProdOrderItemQty());
                        productsSvc.updateProduct(product);
                    }
                }

                // 發送通知
                memberNotifySvc.createNotification(
                    order.getMemberId(),
                    "購物訂單編號 " + order.getShopOrderId() + " 因逾時未付款已自動取消。"
                );
            }
        }
    }
}