package com.thestar.payment.controller;

import com.thestar.member.entity.MemberVO;
import com.thestar.order.entity.OrderVO;
import com.thestar.order.repository.OrderRepository;
import com.thestar.payment.service.EcpayService;
import com.thestar.order.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

//金流進出綠界都走這支 一個帶使用者去綠界付款頁 一個接綠界付完款打回來的通知
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

    //進結帳頁 回傳一段會自動送出的html表單 讓瀏覽器直接跳去綠界付款
    @GetMapping(value = "/checkout/{orderId}", produces = MediaType.TEXT_HTML_VALUE)
    public String orderCheckOut(@PathVariable Integer orderId, HttpSession session) {

        //會員從session拿 不信任前端傳的id 避免有人付別人的單
        MemberVO member = (MemberVO) session.getAttribute("loginMember");

        if (member == null) {
            throw new IllegalStateException("尚未登入");
        }
        OrderVO order = orderRepository.findById(orderId).orElseThrow();

        //這張單的擁有者要跟登入的人是同一個 不然擋掉
        if (!order.getMemberId().equals(member.getMemberId())) {
            throw new IllegalArgumentException("無法付款他人訂單");
        }
        String itemName = "theStar訂房";

        //只有待付款0的單能去結帳 已付款或已取消的都不讓再付
        if (order.getOrderStatus() != 0) {
            throw new IllegalStateException("此訂單無法付款（狀態："
                    + order.getOrderStatus() + "）");
        }

        //每次進結帳頁換一個新的金流編號，避免重複結帳時被綠界檔
        String merchantTradeNo = orderService.renewMerchantTradeNo(orderId);

        //送給綠界的金額直接用存好的總額扣掉折扣 這裡只讀不重算 結帳跟回調驗證三邊金額才會一致
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

        //從綠界回傳的參數裡拿出 訂單編號 實付金額 綠界交易編號
        String merchantTradeNo = params.get("MerchantTradeNo");
        Integer paidAmount = Integer.valueOf(params.get("TradeAmt"));
        String ecpayTradeNo = params.get("TradeNo");

        //改訂單狀態為已付款 這裡的(byte)1代表付款方式是信用卡
        //綠界可能重複打同一筆回調 重複打進來的就丟例外
        //所以這裡catch起來一樣回1|OK 讓綠界不要一直重送
        try {
            orderService.confirmOrder(merchantTradeNo, paidAmount, (byte) 1, ecpayTradeNo);

        } catch (IllegalStateException e) {

            return "1|OK";
        }
        //收到有效通知一定要回 1|OK 否則判定失敗一直重送
        return "1|OK";
    }


}
