package com.example.rentcar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.rentcar.service.RecaptchaService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final RecaptchaFilter recaptchaFilter;

    public SecurityConfig(RecaptchaService recaptchaService) {
        // Inicializamos o filtro com o serviço
        this.recaptchaFilter = new RecaptchaFilter(recaptchaService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Necessário para APIs REST (React)
            .authorizeHttpRequests(auth -> auth
                // Rotas públicas da nossa nova API
                .requestMatchers("/api/auth/**").permitAll()
                // Tudo o resto exige autenticação
                .anyRequest().authenticated()
            )
            // Adicionamos o nosso filtro de reCAPTCHA antes do filtro de autenticação
            .addFilterBefore(recaptchaFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // Este Bean permite-nos injetar o AuthenticationManager no nosso AuthController futuro
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}