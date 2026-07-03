package com.example.thestar1.order.controller;

import com.example.thestar1.order.entity.OrderVO;
import com.example.thestar1.order.repository.OrderRepository;
import com.example.thestar1.order.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FakeOrderConfirm {

    private final OrderService orderService;
    private final OrderRepository orderRepository;


    public FakeOrderConfirm(OrderService orderService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/dev/confirm/{orderId}")
    public String fakeConfirm(@PathVariable Integer orderId) {
        OrderVO order = orderRepository.findById(orderId).orElseThrow();

        String merChantTradeNo = order.getMerchantTradeNo();

        int totalAmount = order.getTotalAmount();

        String ecPay = "dev" + System.currentTimeMillis();

        orderService.confirmOrder(merChantTradeNo, totalAmount, (byte) 1, ecPay);

        return "fake confirm ok OrderId=" + orderId;
    }
}