package com.example.thestar1.member.controller;

import com.example.thestar1.member.entity.MemberVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FakeLoginController {

    @GetMapping("/dev/login/{memberId}")
    public String fakeLogin(@PathVariable Integer memberId, HttpSession session) {
        MemberVO member = new MemberVO();
        member.setMemberId(memberId);
        session.setAttribute("loginMember", member);
        return "fake login ok, memberId=" + memberId + ", sessionId=" + session.getId();
    }

    @GetMapping("/dev/employeelogin/{employeeId}")
    public  String fakeEmployeeLogin(@PathVariable Integer employeeId, HttpSession session){
        session.setAttribute("loginEmployee", employeeId);
        return"fake employee login OK employeeId = " + employeeId;
    }

}