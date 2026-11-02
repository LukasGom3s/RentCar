package com.example.rentcar.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.example.rentcar.models.Administrador;
import com.example.rentcar.repository.UsuarioRepository;

@Component
public class AdminBootstrapInitializer implements ApplicationRunner {

    private final String email;
    private final String password;
    private final String name;
    private final UsuarioRepository usuarioRepository;
    private final UserService userService;

    public AdminBootstrapInitializer(
            @Value("${app.admin.email:}") String email,
            @Value("${app.admin.password:}") String password,
            @Value("${app.admin.name:}") String name,
            UsuarioRepository usuarioRepository,
            UserService userService) {
        this.email = email.trim();
        this.password = password;
        this.name = name.trim();
        this.usuarioRepository = usuarioRepository;
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean hasEmail = !email.isBlank();
        boolean hasPassword = password != null && !password.isBlank();
        boolean hasName = !name.isBlank();

        if (!hasEmail && !hasPassword && !hasName) {
            return;
        }
        if (!hasEmail || !hasPassword || !hasName) {
            throw new IllegalStateException(
                    "Configure APP_ADMIN_EMAIL, APP_ADMIN_PASSWORD e APP_ADMIN_NAME em conjunto.");
        }
        if (password.length() < 12) {
            throw new IllegalStateException("APP_ADMIN_PASSWORD deve ter pelo menos 12 caracteres.");
        }

        try {
            userService.createAdministrator(email, password, name);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(exception.getMessage(), exception);
        }
    }
}
