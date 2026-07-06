package com.thestar.member.service;

import com.thestar.member.entity.MemberVO;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Service
public class MemberVerificationMailService {

    // 這裡保留你原本 MailService 的概念：Gmail SSL 465 直接寄信。
    // 不要把真正密碼上傳到 GitHub，正式值放 application.properties。
    @Value("${app.mail.gmail:my@Gmail}")
    private String myGmail;

    @Value("${app.mail.gmail-password:myGmailPassword}")
    private String myGmailPassword;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    public String buildVerifyUrl(String token) {
        String baseUrl = appBaseUrl == null || appBaseUrl.isBlank()
                ? "http://localhost:8080"
                : appBaseUrl.trim();

        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        return baseUrl + "/verify.html?token=" + encodedToken;
    }

    public boolean sendVerifyMail(MemberVO member, String verifyUrl) {
        try {
            sendVerificationMail(member.getMemberEmail(), member.getMemberName(), verifyUrl);
            return true;
        } catch (Exception e) {
            System.out.println("會員新增成功，但驗證信寄送失敗");
            e.printStackTrace();
            return false;
        }
    }

    // 對應你原本的 sendVerificationMail(String to, String memberName, String verifyLink)
    public void sendVerificationMail(String to, String memberName, String verifyLink) {
        String subject = "The Star Hotel 會員信箱驗證";

        String safeName = memberName == null || memberName.isBlank() ? "會員" : memberName;

        String messageText =
                safeName + " 您好：\n\n"
                        + "請點選以下連結完成會員信箱驗證：\n"
                        + verifyLink + "\n\n"
                        + "此驗證連結將於 10 分鐘後失效。\n\n"
                        + "The Star Hotel";

        sendMail(to, subject, messageText);
    }

    // 對應你原本的 sendMail(String to, String subject, String messageText)
    public void sendMail(String to, String subject, String messageText) {
        if (myGmail == null || myGmail.isBlank()
                || myGmailPassword == null || myGmailPassword.isBlank()
                || "my@Gmail".equals(myGmail)
                || "myGmailPassword".equals(myGmailPassword)) {
            throw new IllegalStateException("尚未設定 Gmail 帳號或應用程式密碼");
        }

        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "465");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(myGmail, myGmailPassword);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(myGmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

            message.setSubject(subject, "UTF-8");
            message.setText(messageText, "UTF-8");

            Transport.send(message);
            System.out.println("驗證信傳送成功：" + to);

        } catch (MessagingException e) {
            System.out.println("驗證信傳送失敗：" + to);
            throw new IllegalStateException("驗證信傳送失敗", e);
        }
    }
}
