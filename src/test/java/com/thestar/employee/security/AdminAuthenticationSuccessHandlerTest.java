package com.thestar.employee.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminAuthenticationSuccessHandlerTest {

    @Test
    void apiSavedRequestIsDiscardedAfterLogin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest apiRequest = new MockHttpServletRequest(
                "GET", "/thestar/admin/me");
        apiRequest.setSession(session);
        apiRequest.addHeader("Accept", "application/json");
        new HttpSessionRequestCache().saveRequest(apiRequest, new MockHttpServletResponse());

        MockHttpServletRequest loginRequest = new MockHttpServletRequest(
                "POST", "/thestar/admin/login");
        loginRequest.setSession(session);
        MockHttpServletResponse loginResponse = new MockHttpServletResponse();
        Authentication authentication = new TestingAuthenticationToken(new Object(), null);

        AdminAuthenticationSuccessHandler handler = new AdminAuthenticationSuccessHandler(null);
        handler.onAuthenticationSuccess(loginRequest, loginResponse, authentication);

        assertEquals("/thestar/admin/home", loginResponse.getRedirectedUrl());
    }
}
