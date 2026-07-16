package com.thestar.member.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class MemberLoginFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = getPath(request);

        if (!needsMemberLogin(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        Object loginMember = session == null ? null : session.getAttribute("loginMember");

        if (loginMember != null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isApiRequest(path, request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"請先登入會員\"}");
            return;
        }

        String redirectTarget = buildRedirectTarget(request, path);
        String loginUrl = request.getContextPath()
                + "/login.html?redirect="
                + URLEncoder.encode(redirectTarget, StandardCharsets.UTF_8);

        response.sendRedirect(loginUrl);
    }

    private String getPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }

        return uri;
    }

    private boolean needsMemberLogin(String path) {
        return path.equals("/profile.html")
                || path.equals("/api/member/profile")
                || path.startsWith("/api/member/profile/")
                || path.equals("/api/member/coupons")
                || path.startsWith("/api/member/notifications")
                || path.equals("/coupons.html")
                || path.equals("/thestar/order/create")
                || path.startsWith("/thestar/order/member")
                || path.startsWith("/thestar/order/cancel")
                || path.startsWith("/thestar/ecpay/checkout")
                || path.equals("/shop/cart")
                || path.startsWith("/shop/cart/")
                || path.equals("/restaurant/booking")
                || path.startsWith("/restaurant/booking/")
                || path.equals("/restaurant/review/add")
                || path.equals("/restaurant/submitReview");
        		
    }

    private boolean isApiRequest(String path, HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");
        String fetchDestination = request.getHeader("Sec-Fetch-Dest");

        return path.equals("/api")
                || path.startsWith("/api/")
                || path.contains("/api/")
                || path.startsWith("/thestar/")
                || "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || "empty".equalsIgnoreCase(fetchDestination)
                || (accept != null && accept.contains("application/json"));
    }

    private String buildRedirectTarget(HttpServletRequest request, String path) {
        if (!"GET".equalsIgnoreCase(request.getMethod())
                && !"HEAD".equalsIgnoreCase(request.getMethod())) {
            return getPostLoginLandingPage(path);
        }

        String queryString = request.getQueryString();

        if (queryString == null || queryString.isBlank()) {
            return path;
        }

        return path + "?" + queryString;
    }
    
    private String getPostLoginLandingPage(String path) {
        if (path.equals("/shop/cart") || path.startsWith("/shop/cart/")) {
            return "/shop/cart";
        }

        if (path.startsWith("/thestar/order/")
                || path.startsWith("/thestar/ecpay/")) {
            return "/roombooking.html";
        }
        
        if (path.equals("/restaurant/booking")
                || path.startsWith("/restaurant/booking/")
                || path.startsWith("/restaurant/ubmitReview/")) {
            return "/restaurant/booking/add";
        }


        return "/index.html";
    }
}
