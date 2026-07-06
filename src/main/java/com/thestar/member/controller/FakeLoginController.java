package com.thestar.member.controller;

import com.thestar.member.entity.MemberVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class FakeLoginController {

    @GetMapping("/dev/login/{memberId}")
    public String fakeLogin(@PathVariable Integer memberId, HttpSession session) {
        MemberVO member = new MemberVO();
        member.setMemberId(memberId);
        session.setAttribute("loginMember", member);
        // 切換身分:登入會員就清掉員工，避免 session 同時有兩種身分
        session.removeAttribute("loginEmployee");
        return "fake login ok, memberId=" + memberId + ", sessionId=" + session.getId();
    }

    @GetMapping("/dev/employeelogin/{employeeId}")
    public  String fakeEmployeeLogin(@PathVariable Integer employeeId, HttpSession session){
        session.setAttribute("loginEmployee", employeeId);
        // 切換身分:登入員工就清掉會員，避免重整後 /dev/me 還原成會員
        session.removeAttribute("loginMember");
        return"fake employee login OK employeeId = " + employeeId;
    }

    @GetMapping("/dev/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "logout ok";
    }

    // 回傳 session 裡目前的登入者，讓前端重整後能還原登入狀態
    @GetMapping("/dev/me")
    public Map<String, Object> me(HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute("loginMember");
        Map<String, Object> result = new HashMap<>();
        result.put("memberId", member != null ? member.getMemberId() : null);
        result.put("employeeId", session.getAttribute("loginEmployee"));
        return result;
    }

}