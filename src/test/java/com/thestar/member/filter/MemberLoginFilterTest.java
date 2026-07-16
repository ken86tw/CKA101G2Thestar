package com.thestar.member.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MemberLoginFilterTest {

    private final MemberLoginFilter filter = new MemberLoginFilter();

    @Test
    void nestedApiPathReturnsJsonInsteadOfCreatingLoginRedirect() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET", "/restaurant/booking/api/session-status");
        request.addHeader("Accept", "*/*");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean();

        filter.doFilter(request, response, (req, res) -> chainCalled.set(true));

        assertEquals(401, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        assertEquals("{\"error\":\"請先登入會員\"}", response.getContentAsString());
        assertFalse(chainCalled.get());
    }

    @Test
    void protectedHtmlPageStillRedirectsToLoginPage() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET", "/restaurant/booking/add");
        request.addHeader("Accept", "text/html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> { });

        assertEquals(302, response.getStatus());
        assertEquals(
                "/login.html?redirect=%2Frestaurant%2Fbooking%2Fadd",
                response.getRedirectedUrl());
    }
}
