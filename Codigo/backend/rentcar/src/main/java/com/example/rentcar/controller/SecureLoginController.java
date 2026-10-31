package com.example.rentcar.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import com.example.rentcar.service.PasswordRecoveryService;
import com.example.rentcar.service.SendEmailService;
import com.example.rentcar.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
// Opcional: Adicionar @CrossOrigin("http://localhost:5173") quando o React estiver a correr
public class SecureLoginController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordRecoveryService passwordRecoveryService;
    private final SendEmailService sendEmailService;

    public SecureLoginController(
            AuthenticationManager authenticationManager,
            UserService userService,
            PasswordRecoveryService passwordRecoveryService,
            SendEmailService sendEmailService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.passwordRecoveryService = passwordRecoveryService;
        this.sendEmailService = sendEmailService;
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credentials.get("email"),
                            credentials.get("senha")
                    )
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("mensagem", "Login efetuado com sucesso!");
            response.put("usuario", auth.getName());
            
            return ResponseEntity.ok(response);
            
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
        
        if (userService.exists(email)) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", "E-mail já registado.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        userService.createUser(
                email,
                dadosCadastro.get("senha"),
                dadosCadastro.get("nome")
        );

        Map<String, String> response = new HashMap<>();
        response.put("mensagem", "Utilizador registado com sucesso!");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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