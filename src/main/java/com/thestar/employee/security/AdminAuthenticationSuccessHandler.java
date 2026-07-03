package com.example.thestar1.employee.security;

import com.example.thestar1.employee.service.EmployeeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 登入成功後：
 * 1) 沿用既有 session 慣例，設定 loginEmployee，讓訂單/住宿等既有後台 Controller 不需修改即可運作。
 * 2) 更新 EMPLOYEE.LAST_LOGIN_TIME。
 * 3) 交還給父類別處理「登入前若有原欲訪問頁面，優先導回該頁」的標準行為。
 */
@Component
public class AdminAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final EmployeeService employeeService;

    public AdminAuthenticationSuccessHandler(EmployeeService employeeService) {
        this.employeeService = employeeService;
        setDefaultTargetUrl("/thestar/admin/home");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws ServletException, IOException {
        if (authentication.getPrincipal() instanceof EmployeeUserDetails principal) {
            request.getSession().setAttribute("loginEmployee", principal.getEmployeeId());
            employeeService.updateLastLoginTime(principal.getEmployeeId());
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
