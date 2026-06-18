package com.example.thestar1;
import com.example.thestar1.payment.service.EcpayService;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class EcpayServiceTest {

    @Test
    void genCheckMacValue_應符合綠界官方範例() {
        //注意：用官方範例自己那組金鑰，不是 application.properties 那組測試商店金鑰
        EcpayService ecpayService = new EcpayService("pwFHCqoQZGmho4w6",
                "EkRm7iFT261dpevs","","","","");

        Map<String, String> params = new HashMap<>();
        params.put("ChoosePayment", "ALL");
        params.put("EncryptType", "1");
        params.put("ItemName", "Apple iphone 15");
        params.put("MerchantID", "3002607");
        params.put("MerchantTradeDate", "2023/03/12 15:30:23");
        params.put("MerchantTradeNo", "ecpay20230312153023");
        params.put("PaymentType", "aio");
        params.put("ReturnURL", "https://www.ecpay.com.tw/receive.php");
        params.put("TotalAmount", "30000");
        params.put("TradeDesc", "促銷方案");

        //綠界官方文件算出的正確答案
        String expected =
                "6C51C9E6888DE861FD62FB1DD17029FC742634498FD813DC43D4243B5685B840";

        assertEquals(expected, ecpayService.genCheckMacValue(params));
    }
}

