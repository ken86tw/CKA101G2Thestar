package com.thestar.payment.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

//算章 驗章 組結帳表單
@Service
public class EcpayService {

    //用@Value從application.properties注入
    private final String hashKey;
    private final String hashIv;
    private final String merchantId;
    private final String aioUrl;
    private final String returnUrl;
    private final String clientBackUrl;

    public EcpayService(@Value("${ecpay.hash-key}") String hashKey,
                        @Value("${ecpay.hash-iv}") String hashIv,
                        @Value("${ecpay.merchant-id}") String merchantId,
                        @Value("${ecpay.aio-url}") String aioUrl,
                        @Value("${ecpay.return-url}") String returnUrl,
                        @Value("${ecpay.client-back-url}") String clientBackUrl) {
        this.hashIv = hashIv;
        this.hashKey = hashKey;
        this.merchantId = merchantId;
        this.aioUrl = aioUrl;
        this.returnUrl = returnUrl;
        this.clientBackUrl = clientBackUrl;

    }

    //算綠界的檢查碼CheckMacValue
    public String genCheckMacValue(Map<String, String> params) {

        //先把所有參數依參數名A到Z排序 用CASE_INSENSITIVE不分大小寫 綠界規定
        TreeMap<String, String> sorted = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        sorted.putAll(params);

        //CheckMacValue本身不參與計算 要先拿掉
        sorted.remove("CheckMacValue");

        //排好序後串成 key=value&key=value 的字串
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
        }
        //頭尾夾上HashKey跟HashIV
        String raw = "HashKey=" + hashKey + "&" + sb + "HashIV=" + hashIv;

        //整串做urlencode後轉小寫
        String encoded = urlEncode(raw).toLowerCase();

        //最後做SHA256雜湊再轉大寫 變成檢查碼
        return sha256(encoded).toUpperCase();

    }

    //驗綠界打回來的檢查碼 拿它給的CheckMacValue跟我自己用同一套算法算出來的比
    public boolean verifyCheckValue(Map<String, String> params) {
        String received = params.get("CheckMacValue");
        if (received == null) {
            return false;
        }
        return received.equalsIgnoreCase(genCheckMacValue(params));
    }

    //組出要送去綠界的結帳表單 把訂單資訊塞成綠界要的參數 算好章 再包成一段會自動送出的html
    public String buildCheckoutForm(String merChantTradeNo, int totalAmount, String itemName, Integer orderId){
        //綠界必填參數一個一個放進map
        Map<String, String>params = new HashMap<>();
        params.put("MerchantID", merchantId);
        params.put("MerchantTradeNo", merChantTradeNo);
        params.put("MerchantTradeDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
        params.put("PaymentType","aio");
        params.put("TotalAmount",String.valueOf(totalAmount));
        params.put("TradeDesc", "TheStar訂房");
        params.put("ItemName", itemName);
        params.put("ReturnURL",returnUrl);
        //付完款使用者點「返回商店」導回前端，帶上 orderId 讓結果頁知道是哪張訂單
        params.put("ClientBackURL", clientBackUrl + "?orderId=" + orderId);
        params.put("ChoosePayment","Credit");
        params.put("EncryptType","1");

        //隨後呼叫genCheckMacValue方法要
        // 參數都放好後最後才算簽章 因為簽章要用到上面全部的參數
        params.put("CheckMacValue", genCheckMacValue(params));

        //把每個參數變成隱藏欄位塞進form 再用一段script讓瀏覽器一載入就自動送出跳去綠界
        StringBuilder form = new StringBuilder();

        form.append("<form id=\"ecpay\" method=\"post\" action=\"").append(aioUrl).append("\">");
        for (Map.Entry<String, String> e : params.entrySet()) {
            form.append("<input type=\"hidden\" name=\"")
                    .append(e.getKey()).append("\" value=\"")
                    .append(e.getValue()).append("\"/>");
        }
        form.append("</form>");
        form.append("<script>document.getElementById('ecpay').submit();</script>");
        return form.toString();
    }


    //綠界的urlencode跟java預設不一樣 java會把這幾個字元也編碼 但綠界不編 所以要手動換回來 不然算出來的章會對不上
    private String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8)
                .replace("%21", "!")
                .replace("%2A", "*")
                .replace("%28", "(")
                .replace("%29", ")")
                .replace("%2D", "-")
                .replace("%2E", ".")
                .replace("%5F", "_");
    }

    //SHA256雜湊 把字串轉成16進位的小寫字串
    private String sha256(String s) {

        try {

            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();


        } catch (Exception e) {
            throw new IllegalStateException("CheckMcaValue 計算失敗", e);
        }

    }

}
