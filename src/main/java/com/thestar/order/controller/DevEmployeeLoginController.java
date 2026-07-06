package com.thestar.order.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DevEmployeeLoginController {

    @GetMapping("/dev/employeelogin/{employeeId}")
    public Map<String, Object> employeeLogin(@PathVariable Integer employeeId, HttpSession session) {
        if (employeeId == null || employeeId <= 0) {
            throw new IllegalArgumentException("員工 ID 不正確");
        }

        session.setAttribute("loginEmployee", employeeId);
        return Map.of("ok", true, "employeeId", employeeId);
    }
}
