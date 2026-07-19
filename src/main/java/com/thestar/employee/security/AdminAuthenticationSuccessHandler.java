package com.thestar.employee.security;

import com.thestar.employee.service.EmployeeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * 登入成功後：
 * 1) 沿用既有 session 慣例，設定 loginEmployee，讓訂單/住宿等既有後台 Controller 不需修改即可運作。
 * 2) 更新 EMPLOYEE.LAST_LOGIN_TIME。
 * 3) 交還給父類別處理「登入前若有原欲訪問頁面，優先導回該頁」的標準行為。
 */
@Component
public class AdminAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final EmployeeService employeeService;
    private final RequestCache requestCache = new HttpSessionRequestCache();

    public AdminAuthenticationSuccessHandler(EmployeeService employeeService) {
        this.employeeService = employeeService;
        setDefaultTargetUrl("/thestar/admin/home");
        setRequestCache(requestCache);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws ServletException, IOException {
        if (authentication.getPrincipal() instanceof EmployeeUserDetails principal) {
            request.getSession().setAttribute("loginEmployee", principal.getEmployeeId());
            employeeService.updateLastLoginTime(principal.getEmployeeId());
        }

        SavedRequest savedRequest = requestCache.getRequest(request, response);

        // 訂房後台的資料 API(訂單/住宿/退款/庫存)被攔下存成 saved request 的話,
        // 登入後不能照標準行為導回去——那些網址回的是 JSON,瀏覽器直接開會變成一整頁 JSON 字。
        // 這些功能的操作畫面都在訂房頁,所以一律改導回 /roombooking.html
        String roomBookingLanding = roomBookingLandingPage(savedRequest, request.getContextPath());
        if (roomBookingLanding != null) {
            requestCache.removeRequest(request, response);
            getRedirectStrategy().sendRedirect(request, response, roomBookingLanding);
            return;
        }

        if (isApiRequest(savedRequest, request.getContextPath())) {
            requestCache.removeRequest(request, response);
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }

    /**
     * saved request 是訂房後台的資料 API 時,回傳應改導的頁面(/roombooking.html);
     * 不是的話回傳 null,交回原本的流程處理。
     */
    private String roomBookingLandingPage(SavedRequest savedRequest, String contextPath) {
        if (savedRequest == null) {
            return null;
        }

        try {
            String path = URI.create(savedRequest.getRedirectUrl()).getPath();
            if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
                path = path.substring(contextPath.length());
            }

            if (path.startsWith("/thestar/admin/stayrecord")
                    || path.startsWith("/thestar/admin/order")
                    || path.startsWith("/thestar/admin/refund")
                    || path.equals("/find/admin/room")) {
                return "/roombooking.html";
            }
        } catch (IllegalArgumentException e) {
            return null;
        }

        return null;
    }

    private boolean isApiRequest(SavedRequest savedRequest, String contextPath) {
        if (savedRequest == null) {
            return false;
        }

        if (headerContains(savedRequest, "Accept", "application/json")
                || headerContains(savedRequest, "X-Requested-With", "XMLHttpRequest")
                || headerContains(savedRequest, "Sec-Fetch-Dest", "empty")) {
            return true;
        }

        try {
            String path = URI.create(savedRequest.getRedirectUrl()).getPath();
            if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
                path = path.substring(contextPath.length());
            }

            return path.equals("/thestar/admin/me")
                    || path.equals("/thestar/admin/employee")
                    || path.matches("/thestar/admin/employee/\\d+")
                    || path.equals("/thestar/admin/role")
                    || path.startsWith("/thestar/admin/role/")
                    || path.equals("/thestar/admin/permission")
                    || path.startsWith("/thestar/admin/permission/");
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private boolean headerContains(SavedRequest savedRequest, String name, String expectedValue) {
        List<String> values = savedRequest.getHeaderValues(name);
        return values != null && values.stream()
                .anyMatch(value -> value != null && value.toLowerCase().contains(expectedValue.toLowerCase()));
    }
}
