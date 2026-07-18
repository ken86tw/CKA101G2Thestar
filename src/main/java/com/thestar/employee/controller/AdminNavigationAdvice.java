package com.thestar.employee.controller;

import com.thestar.employee.security.EmployeeUserDetails;
import com.thestar.employee.security.RoleCodes;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Set;
import java.util.stream.Collectors;

/** Makes the same navigation permissions available to every server-rendered admin page. */
@ControllerAdvice
public class AdminNavigationAdvice {

    @ModelAttribute
    public void addAdminNavigationPermissions(org.springframework.ui.Model model,
                                              Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof EmployeeUserDetails principal)) {
            addPermissions(model, Set.of());
            return;
        }

        Set<String> authorities = principal.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet());
        addPermissions(model, authorities);
        model.addAttribute("currentEmployeeName", principal.getEmployee().getEmployeeName());
    }

    private void addPermissions(org.springframework.ui.Model model, Set<String> authorities) {
        boolean superAdmin = authorities.contains(RoleCodes.SUPER_ADMIN);
        model.addAttribute("canManageEmployees", superAdmin || authorities.contains(RoleCodes.HR));
        model.addAttribute("canUseFrontDesk", superAdmin || authorities.contains(RoleCodes.FRONT_DESK));
        model.addAttribute("canUseRestaurant", superAdmin || authorities.contains(RoleCodes.RESTAURANT_STAFF));
        model.addAttribute("canUseShop", superAdmin || authorities.contains(RoleCodes.PRODUCT_ADMIN));
        model.addAttribute("canUseContent", superAdmin || authorities.contains(RoleCodes.CONTENT_ADMIN));
        model.addAttribute("canUseMembers", superAdmin || authorities.contains(RoleCodes.MEMBER_ADMIN));
    }
}
