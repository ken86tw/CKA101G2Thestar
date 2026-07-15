package com.thestar.common.controller;

import com.thestar.content.service.ContentAdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontPageController {

    private final ContentAdminService contentAdminService;

    public FrontPageController(ContentAdminService contentAdminService) {
        this.contentAdminService = contentAdminService;
    }

    @GetMapping({"/", "/index.html"})
    public String index(Model model) {
        model.addAttribute("latestNews", contentAdminService.findLatestNews());
        return "index";
    }

    @GetMapping("/facilities.html")
    public String facilities() {
        return "facilities";
    }

    @GetMapping("/coupons.html")
    public String coupons() {
        return "coupons";
    }
}
