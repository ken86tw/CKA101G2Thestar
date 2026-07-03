package com.example.thestar1.employee.controller;

import com.example.thestar1.employee.security.EmployeeUserDetails;
import com.example.thestar1.employee.security.PermissionCodes;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/thestar/admin")
public class AdminHomeController {

    @GetMapping("/home")
    public String home(Model model, @AuthenticationPrincipal EmployeeUserDetails principal) {
        model.addAttribute("currentEmployeeName", principal.getEmployee().getEmployeeName());
        model.addAttribute("employeeMail", principal.getUsername());
        model.addAttribute("canManageEmployees", principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(PermissionCodes.EMPLOYEE_MANAGE)));
        model.addAttribute("canManageRoles", principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")));
        model.addAttribute("canUseStayRecord", principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(PermissionCodes.STAYRECORD_VIEW)
                        || a.getAuthority().equals(PermissionCodes.STAYRECORD_CHECKIN)));
        model.addAttribute("canUseOrders", principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(PermissionCodes.ORDER_VIEW)
                        || a.getAuthority().equals(PermissionCodes.ORDER_REFUND)));
        return "admin/home";
    }
}
