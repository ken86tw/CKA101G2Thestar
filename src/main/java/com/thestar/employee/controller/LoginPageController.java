package com.example.thestar1.employee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/thestar/admin")
public class LoginPageController {

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }
}
