package com.example.rentcar.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import com.example.rentcar.config.UserConfig;
import com.example.rentcar.service.PasswordRecoveryService;
import com.example.rentcar.service.SendEmailService;
import com.example.rentcar.service.UserService;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class SecureLoginController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordRecoveryService passwordRecoveryService;
    private final SendEmailService sendEmailService;
    private final SecurityContextRepository securityContextRepository;
    private final UserConfig userConfig;

    public SecureLoginController(
            AuthenticationManager authenticationManager,
            UserService userService,
            PasswordRecoveryService passwordRecoveryService,
            SendEmailService sendEmailService,
            SecurityContextRepository securityContextRepository,
            UserConfig userConfig) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.passwordRecoveryService = passwordRecoveryService;
        this.sendEmailService = sendEmailService;
        this.securityContextRepository = securityContextRepository;
        this.userConfig = userConfig;
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @GetMapping("/recaptcha-site-key")
    public Map<String, String> recaptchaSiteKey() {
        return Map.of("siteKey", userConfig.getRecaptchaSiteKey());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> credentials,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credentials.get("email"),
                            credentials.get("senha")
                    )
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("mensagem", "Login efetuado com sucesso!");
            responseBody.put("usuario", auth.getName());

            return ResponseEntity.ok().body(responseBody);

        } catch (AuthenticationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", "Credenciais inválidas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    // =========================================================
    // CADASTRO
    // =========================================================

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> dadosCadastro) {
        String email = dadosCadastro.get("email");

        if (isBlank(email)
                || isBlank(dadosCadastro.get("senha"))
                || isBlank(dadosCadastro.get("nome"))
                || isBlank(dadosCadastro.get("rg"))
                || isBlank(dadosCadastro.get("cpf"))
                || isBlank(dadosCadastro.get("morada"))
                || isBlank(dadosCadastro.get("profissao"))) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", "Preencha todos os campos obrigatórios.");
            return ResponseEntity.badRequest().body(error);
        }

        if (userService.exists(email)) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", "E-mail já registado.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        userService.createUser(
                email,
                dadosCadastro.get("senha"),
                dadosCadastro.get("nome"),
                dadosCadastro.get("rg"),
                dadosCadastro.get("cpf"),
                dadosCadastro.get("morada"),
                dadosCadastro.get("profissao")
        );

        Map<String, String> response = new HashMap<>();
        response.put("mensagem", "Utilizador registado com sucesso!");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // =========================================================
    // RECUPERAÇÃO DE SENHA
    // =========================================================

    @PostMapping("/recoverpassword")
    public ResponseEntity<?> recoverPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (!userService.exists(email)) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", "E-mail não encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        String token = passwordRecoveryService.generateToken(email);
        String linkReact = "http://localhost:5173/resetpassword?token=" + token;

        sendEmailService.sendEmail(
                email,
                "Recuperação de Senha - Aluguer de Carros",
                "Clique aqui para redefinir a sua senha: " + linkReact
        );

        Map<String, String> response = new HashMap<>();
        response.put("mensagem", "E-mail de recuperação enviado.");
        return ResponseEntity.ok(response);
    }

    // =========================================================
    // RESET DE SENHA
    // =========================================================

    @PostMapping("/resetpassword")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String senha = request.get("senha");
        String confirmarSenha = request.get("confirmarSenha");

        if (!senha.equals(confirmarSenha)) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", "As senhas não coincidem.");
            return ResponseEntity.badRequest().body(error);
        }

        String email = passwordRecoveryService.getEmailFromToken(token);

        if (email == null) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", "Token inválido ou expirado.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        userService.updatePassword(email, senha);
        passwordRecoveryService.invalidateToken(token);

        Map<String, String> response = new HashMap<>();
        response.put("mensagem", "Senha alterada com sucesso.");
        return ResponseEntity.ok(response);
    }
}