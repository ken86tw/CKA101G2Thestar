package com.thestar.order.controller;

import com.thestar.order.entity.OrderVO;
import com.thestar.order.repository.OrderRepository;
import com.thestar.order.service.OrderService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class FakeOrderConfirm {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;


    public FakeOrderConfirm(OrderService orderService, OrderRepository orderRepository, SimpMessagingTemplate simpMessagingTemplate) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @GetMapping("/dev/confirm/{orderId}")
    public String fakeConfirm(@PathVariable Integer orderId) {
        OrderVO order = orderRepository.findById(orderId).orElseThrow();

        String merChantTradeNo = order.getMerchantTradeNo();

        //結帳是金額為總額減掉折扣價
        int paidAmount = order.getTotalAmount() - order.getDiscountAmount();

        String ecPay = "dev" + System.currentTimeMillis();

        orderService.confirmOrder(merChantTradeNo, paidAmount, (byte) 1, ecPay);

        simpMessagingTemplate.convertAndSend("/topic/orders", (Object) Map.of("event", "paid"));

        return "fake confirm ok OrderId=" + orderId;
    }
}