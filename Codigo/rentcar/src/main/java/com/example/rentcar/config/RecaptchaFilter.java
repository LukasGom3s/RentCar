package com.example.rentcar.config;

import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.rentcar.service.RecaptchaService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RecaptchaFilter extends OncePerRequestFilter {

    private final RecaptchaService recaptchaService;

    public RecaptchaFilter(RecaptchaService recaptchaService) {
        this.recaptchaService = recaptchaService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Interceta pedidos POST para as rotas de autenticação/registo
        if ((request.getRequestURI().equals("/api/auth/login") || request.getRequestURI().equals("/api/auth/register"))
                && request.getMethod().equalsIgnoreCase("POST")) {

            // No React, é comum enviar o token do captcha pelo Header da requisição
            String captchaResponse = request.getHeader("X-Recaptcha-Response");

            boolean captchaValido = recaptchaService.validate(captchaResponse);

            if (!captchaValido) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"erro\": \"Validação reCAPTCHA falhou. Tente novamente.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}