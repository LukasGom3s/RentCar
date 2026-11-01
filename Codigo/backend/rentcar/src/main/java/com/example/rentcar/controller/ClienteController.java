package com.example.rentcar.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.rentcar.models.Cliente;
import com.example.rentcar.service.ClienteService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/clientes/me")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ClienteResponse getProfile(@AuthenticationPrincipal UserDetails user) {
        return ClienteResponse.from(clienteService.getByLogin(user.getUsername()));
    }

    @PutMapping
    public ClienteResponse updateProfile(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody ClienteUpdateRequest request) {
        Cliente updated = clienteService.update(
                user.getUsername(),
                request.nome(),
                request.rg(),
                request.cpf(),
                request.morada(),
                request.profissao());
        return ClienteResponse.from(updated);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteProfile(
            @AuthenticationPrincipal UserDetails user,
            HttpServletRequest request) {
        clienteService.delete(user.getUsername());
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    public record ClienteUpdateRequest(
            String nome,
            String rg,
            String cpf,
            String morada,
            String profissao) {
    }

    public record ClienteResponse(
            String email,
            String nome,
            String rg,
            String cpf,
            String morada,
            String profissao) {
        static ClienteResponse from(Cliente cliente) {
            return new ClienteResponse(
                    cliente.getLogin(),
                    cliente.getNome(),
                    cliente.getRg(),
                    cliente.getCpf(),
                    cliente.getMorada(),
                    cliente.getProfissao());
        }
    }
}
