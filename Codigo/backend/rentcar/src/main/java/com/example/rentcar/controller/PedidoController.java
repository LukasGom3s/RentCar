package com.example.rentcar.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.rentcar.models.Pedido;
import com.example.rentcar.models.StatusPedido;
import com.example.rentcar.service.PedidoService;

import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/pedidos/me")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<PedidoResponse> findMyPedidos(@AuthenticationPrincipal UserDetails user) {
        return pedidoService.findMyPedidos(user.getUsername()).stream()
                .map(PedidoResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse create(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody PedidoCreateRequest request) {
        Pedido pedido = pedidoService.create(
                user.getUsername(),
                request.automovelId(),
                request.dataInicio(),
                request.dataFim());
        return PedidoResponse.from(pedido);
    }

    @PutMapping("/{pedidoId}")
    public PedidoResponse update(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long pedidoId,
            @RequestBody PedidoCreateRequest request) {
        Pedido pedido = pedidoService.update(
                user.getUsername(),
                pedidoId,
                request.automovelId(),
                request.dataInicio(),
                request.dataFim());
        return PedidoResponse.from(pedido);
    }

    @PatchMapping("/{pedidoId}/cancelamento")
    public PedidoResponse cancel(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long pedidoId) {
        return PedidoResponse.from(pedidoService.cancel(user.getUsername(), pedidoId));
    }

    public record PedidoCreateRequest(
            Long automovelId,
            LocalDate dataInicio,
            LocalDate dataFim) {
    }

    public record PedidoResponse(
            Long id,
            Long automovelId,
            String matricula,
            int ano,
            String marca,
            String modelo,
            String placa,
            LocalDate dataInicio,
            LocalDate dataFim,
            LocalDateTime dataCriacao,
            String parecerFinanceiro,
            StatusPedido status) {
        static PedidoResponse from(Pedido pedido) {
            var automovel = pedido.getAutomovel();
            return new PedidoResponse(
                    pedido.getId(),
                    automovel.getId(),
                    automovel.getMatricula(),
                    automovel.getAno(),
                    automovel.getMarca(),
                    automovel.getModelo(),
                    automovel.getPlaca(),
                    pedido.getDataInicio(),
                    pedido.getDataFim(),
                    pedido.getDataCriacao(),
                    pedido.getParecerFinanceiro(),
                    pedido.getStatus());
        }
    }
}
