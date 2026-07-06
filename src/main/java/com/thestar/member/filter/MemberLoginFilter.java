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
        return path.equals("/thestar/order/create")
                || path.startsWith("/thestar/order/member")
                || path.startsWith("/thestar/order/cancel")
                || path.startsWith("/thestar/ecpay/checkout");
    }

    private boolean isApiRequest(String path, HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");

        return path.startsWith("/thestar/")
                || path.startsWith("/api/")
                || "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && accept.contains("application/json"));
    }

    private String buildRedirectTarget(HttpServletRequest request, String path) {
        String queryString = request.getQueryString();

        if (queryString == null || queryString.isBlank()) {
            return path;
        }

        return path + "?" + queryString;
    }
}
