package com.thestar.shop.controller.admin;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.thestar.employee.security.EmployeeUserDetails;

public class AdminShopBaseController {

    @ModelAttribute("employeeName")
    public String employeeName(
        @AuthenticationPrincipal EmployeeUserDetails principal) {
        if (principal == null) return "管理員";
        return principal.getEmployee().getEmployeeName();
    }
}