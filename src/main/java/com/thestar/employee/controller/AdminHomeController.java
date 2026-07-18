package com.thestar.employee.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.thestar.employee.security.EmployeeUserDetails;
import com.thestar.employee.security.RoleCodes;

@Controller
@RequestMapping("/thestar/admin")
public class AdminHomeController {

    @GetMapping("/home")
    public String home(Model model, @AuthenticationPrincipal EmployeeUserDetails principal) {
        model.addAttribute("currentEmployeeName", principal.getEmployee().getEmployeeName());
        model.addAttribute("employeeMail", principal.getUsername());
        boolean superAdmin = hasRole(principal, RoleCodes.SUPER_ADMIN);
        model.addAttribute("canManageEmployees", superAdmin || hasRole(principal, RoleCodes.HR));
        model.addAttribute("canManageRoles", superAdmin);
        model.addAttribute("canUseFrontDesk", superAdmin || hasRole(principal, RoleCodes.FRONT_DESK));
        model.addAttribute("canUseRestaurant", superAdmin || hasRole(principal, RoleCodes.RESTAURANT_STAFF));
        model.addAttribute("canUseShop", superAdmin || hasRole(principal, RoleCodes.PRODUCT_ADMIN));
        model.addAttribute("canUseContent", superAdmin || hasRole(principal, RoleCodes.CONTENT_ADMIN));
        model.addAttribute("canUseMembers", superAdmin || hasRole(principal, RoleCodes.MEMBER_ADMIN));
        return "admin/home";
    }

    private boolean hasRole(EmployeeUserDetails principal, String roleCode) {
        return principal.getAuthorities().stream()
                .anyMatch(authority -> roleCode.equals(authority.getAuthority()));
    }
}
