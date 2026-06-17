package com.example.thestar1.controller;

import com.example.thestar1.entity.MemberVO;
import com.example.thestar1.entity.OrderVO;
import com.example.thestar1.repository.OrderRepository;
import com.example.thestar1.service.EcpayService;
import com.example.thestar1.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/thestar/ecpay")
public class EcpayController {

    private final EcpayService ecpayService;
    private final OrderRepository orderRepository;
    private final OrderService orderService;


    public EcpayController(EcpayService ecpayService, OrderRepository orderRepository, OrderService orderService) {
        this.ecpayService = ecpayService;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @GetMapping(value = "/checkout/{orderId}", produces = MediaType.TEXT_HTML_VALUE)
    public String orderCheckOut(@PathVariable Integer orderId, HttpSession session) {

        MemberVO member = (MemberVO) session.getAttribute("loginMember");

        if (member == null) {
            throw new IllegalStateException("尚未登入");
        }
        OrderVO order = orderRepository.findById(orderId).orElseThrow();

        if (!order.getMemberId().equals(member.getMemberId())) {
            throw new IllegalArgumentException("無法付款他人訂單");
        }
        String itemName = "theStar訂房";

        if (order.getOrderStatus() != 0) {
            throw new IllegalStateException("此訂單無法付款（狀態："
                    + order.getOrderStatus() + "）");
        }

        //每次進結帳頁換一個新的金流編號，避免重複結帳時被綠界檔
        String merchantTradeNo = orderService.renewMerchantTradeNo(orderId);

        return ecpayService.buildCheckoutForm(
                merchantTradeNo,
                order.getTotalAmount() - order.getDiscountAmount(),
                itemName,
                orderId);


    }

    //因為綠界是用post打回來給我們接收
    @PostMapping("/return")
    public String ecpayReturn(@RequestParam Map<String, String> params) {

        //先驗checkMacValue
        if (!ecpayService.verifyCheckValue(params)) {
            return "0|CheckMacValue Error";
        }
        //再驗RtnCode RtnCode=1 才是付款成功 不是就不改訂單狀態
        if (!"1".equals(params.get("RtnCode"))) {
            return "1|OK";
        }

        String merchantTradeNo = params.get("MerchantTradeNo");
        Integer paidAmount = Integer.valueOf(params.get("TradeAmt"));
        String ecpayTradeNo = params.get("TradeNo");

        try {
            orderService.confirmOrder(merchantTradeNo, paidAmount, (byte) 1, ecpayTradeNo);

        } catch (IllegalStateException e) {

            return "1|OK";
        }
        return "1|OK";
    }


}
