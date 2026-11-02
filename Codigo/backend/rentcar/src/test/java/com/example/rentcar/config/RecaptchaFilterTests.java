package com.example.rentcar.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.example.rentcar.service.RecaptchaService;

import jakarta.servlet.ServletException;

class RecaptchaFilterTests {

    @Test
    void disabledRecaptchaAllowsAuthenticationRequestWithoutToken() throws ServletException, IOException {
        RecaptchaService recaptchaService = mock(RecaptchaService.class);
        RecaptchaFilter filter = new RecaptchaFilter(recaptchaService, false);
        MockHttpServletRequest request = authRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> chainCalled.set(true));

        assertTrue(chainCalled.get());
        verify(recaptchaService, never()).validate(null);
    }

    @Test
    void enabledRecaptchaRejectsAuthenticationRequestWithoutToken() throws ServletException, IOException {
        RecaptchaService recaptchaService = mock(RecaptchaService.class);
        RecaptchaFilter filter = new RecaptchaFilter(recaptchaService, true);
        MockHttpServletRequest request = authRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> chainCalled.set(true));

        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("reCAPTCHA"));
        assertTrue(!chainCalled.get());
        verify(recaptchaService).validate(null);
    }

    private MockHttpServletRequest authRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/auth/login");
        return request;
    }
}
