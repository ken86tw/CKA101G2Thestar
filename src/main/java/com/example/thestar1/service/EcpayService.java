package com.example.thestar1.service;


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

@Service
public class EcpayService {

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

    public String genCheckMacValue(Map<String, String> params) {
        TreeMap<String, String> sorted = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        sorted.putAll(params);
        sorted.remove("CheckMacValue");
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
        }
        String raw = "HashKey=" + hashKey + "&" + sb + "HashIV=" + hashIv;

        String encoded = urlEncode(raw).toLowerCase();

        return sha256(encoded).toUpperCase();

    }

    public boolean verifyCheckValue(Map<String, String> params) {
        String received = params.get("CheckMacValue");
        if (received == null) {
            return false;
        }
        return received.equalsIgnoreCase(genCheckMacValue(params));
    }

    public String buildCheckoutForm(String merChantTradeNo, int totalAmount, String itemName, Integer orderId){
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

        params.put("CheckMacValue", genCheckMacValue(params));

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
