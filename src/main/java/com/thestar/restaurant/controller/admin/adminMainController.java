package com.thestar.restaurant.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class adminMainController {

    // 讓訪問 http://localhost:8080/admin/restaurant 時導向總首頁
    @GetMapping("/admin/restaurant")
    public String adminIndex() {
        return "admin/index";
    }
}