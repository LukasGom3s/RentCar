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

import org.springframework.beans.factory.annotation.Value;

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
    private final String frontendUrl;

    public SecureLoginController(
            AuthenticationManager authenticationManager,
            UserService userService,
            PasswordRecoveryService passwordRecoveryService,
            SendEmailService sendEmailService,
            SecurityContextRepository securityContextRepository,
            UserConfig userConfig,
            @Value("${app.frontend.url:http://localhost:3000}") String frontendUrl) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.passwordRecoveryService = passwordRecoveryService;
        this.sendEmailService = sendEmailService;
        this.securityContextRepository = securityContextRepository;
        this.userConfig = userConfig;
        this.frontendUrl = frontendUrl;
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

        if (!dadosCadastro.get("cpf").matches("\\d+")) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", "O CPF deve conter somente números.");
            return ResponseEntity.badRequest().body(error);
        }

        if (!dadosCadastro.get("rg").matches("[a-zA-Z0-9]+")) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", "O RG deve conter somente letras e números.");
            return ResponseEntity.badRequest().body(error);
        }

        if (!dadosCadastro.get("senha").equals(dadosCadastro.get("confirmarSenha"))) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", "As senhas não coincidem.");
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

        if (isBlank(email)) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", "Informe o e-mail cadastrado.");
            return ResponseEntity.badRequest().body(error);
        }

        if (!userService.exists(email)) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", "E-mail não encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        String token = passwordRecoveryService.generateToken(email);
        String linkReact = frontendUrl.replaceAll("/+$", "") + "/resetpassword?token=" + token;
        String emailHtml = """
                <!DOCTYPE html>
                <html lang="pt-BR">
                  <body style="margin:0;padding:32px 16px;background-color:#f3f6f9;font-family:Arial,Helvetica,sans-serif;color:#193f61;">
                    <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="max-width:600px;margin:0 auto;background:#ffffff;border:1px solid #e1e7ec;border-radius:16px;overflow:hidden;">
                      <tr>
                        <td style="padding:30px 24px;text-align:center;background:#064ab5;">
                          <div style="font-size:30px;font-weight:700;letter-spacing:1px;color:#ffffff;">RENT<span style="color:#26d0fa;">CAR</span></div>
                          <div style="margin-top:6px;font-size:12px;letter-spacing:3px;color:#ffffff;">ALUGUEL DE VEÍCULOS</div>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:36px 32px 24px;">
                          <h1 style="margin:0 0 18px;font-size:25px;font-weight:600;color:#24577f;">Redefinição de senha</h1>
                          <p style="margin:0 0 16px;font-size:16px;line-height:1.6;color:#52677d;">Recebemos uma solicitação para alterar a senha da sua conta RentCar.</p>
                          <p style="margin:0 0 28px;font-size:16px;line-height:1.6;color:#52677d;">Use o botão abaixo para criar uma nova senha. Este link expira em 15 minutos.</p>
                          <table role="presentation" cellspacing="0" cellpadding="0" style="margin:0 auto 28px;">
                            <tr>
                              <td style="border-radius:8px;background:#24577f;text-align:center;">
                                <a href="{{recoveryLink}}" style="display:inline-block;padding:15px 28px;border-radius:8px;color:#ffffff;font-size:16px;font-weight:700;text-decoration:none;">Redefinir minha senha</a>
                              </td>
                            </tr>
                          </table>
                          <p style="margin:0 0 8px;font-size:13px;line-height:1.6;color:#718198;">Se o botão não funcionar, copie e cole este endereço no navegador:</p>
                          <p style="margin:0;word-break:break-all;font-size:13px;line-height:1.6;"><a href="{{recoveryLink}}" style="color:#24577f;">{{recoveryLink}}</a></p>
                          <p style="margin:28px 0 0;font-size:14px;line-height:1.6;color:#718198;">Se você não solicitou a redefinição, pode ignorar este e-mail. Sua senha permanecerá a mesma.</p>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:18px 24px;text-align:center;background:#f8fafc;color:#91a1b7;font-size:12px;">RentCar · Aluguel de veículos</td>
                      </tr>
                    </table>
                  </body>
                </html>
                """.replace("{{recoveryLink}}", linkReact);

        sendEmailService.sendEmail(
                email,
                "Redefina sua senha - RentCar",
                emailHtml
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

        if (isBlank(token) || isBlank(senha) || isBlank(confirmarSenha)) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", "Token e os dois campos de senha são obrigatórios.");
            return ResponseEntity.badRequest().body(error);
        }

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