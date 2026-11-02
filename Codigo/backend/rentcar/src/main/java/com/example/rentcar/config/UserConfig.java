package com.example.rentcar.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfig {

    // =========================================================
    // GOOGLE reCAPTCHA
    // =========================================================

    @Value("${recaptcha.site-key}")
    private String recaptchaSiteKey;

    @Value("${recaptcha.secret-key}")
    private String recaptchaSecretKey;

    @Value("${recaptcha.enabled:true}")
    private boolean recaptchaEnabled;

    public String getRecaptchaSiteKey() {
        return recaptchaSiteKey;
    }

    public String getRecaptchaSecretKey() {
        return recaptchaSecretKey;
    }

    public boolean isRecaptchaEnabled() {
        return recaptchaEnabled;
    }
}