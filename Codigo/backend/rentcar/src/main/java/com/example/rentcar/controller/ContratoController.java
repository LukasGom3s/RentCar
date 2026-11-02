package com.example.rentcar.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.rentcar.models.Contrato;
import com.example.rentcar.models.StatusPedido;
import com.example.rentcar.service.ContratoService;

@RestController
public class ContratoController {

    private final ContratoService contratoService;

    public ContratoController(ContratoService contratoService) {
        this.contratoService = contratoService;
    }

    @GetMapping("/api/agente/contratos")
    public List<ContratoResponse> findAllForAgent() {
        return contratoService.findAll().stream()
                .map(ContratoResponse::from)
                .toList();
    }

    @PostMapping("/api/agente/contratos/pedidos/{pedidoId}/execucao")
    public ContratoResponse execute(
            @PathVariable Long pedidoId,
            @AuthenticationPrincipal UserDetails user,
            @RequestBody(required = false) ExecuteContractRequest request) {
        boolean agenteBanco = AuthorityUtils.authorityListToSet(user.getAuthorities())
                .contains("ROLE_AGENTE_BANCO");
        Contrato contrato = contratoService.execute(
                pedidoId,
                user.getUsername(),
                agenteBanco,
                request == null ? null : request.detalhesCreditoBanco());
        return ContratoResponse.from(contrato);
    }

    @GetMapping("/api/contratos/me")
    public List<ContratoResponse> findMyContracts(@AuthenticationPrincipal UserDetails user) {
        return contratoService.findMyContracts(user.getUsername()).stream()
                .map(ContratoResponse::from)
                .toList();
    }

    public record ExecuteContractRequest(String detalhesCreditoBanco) {
    }

    public record ContratoResponse(
            Long id,
            Long pedidoId,
            String clienteNome,
            String clienteEmail,
            Long automovelId,
            String matricula,
            int ano,
            String marca,
            String modelo,
            String placa,
            LocalDate dataInicio,
            LocalDate dataFim,
            LocalDateTime dataFormalizacao,
            String agenteLogin,
            String detalhesCreditoBanco,
            StatusPedido status) {
        static ContratoResponse from(Contrato contrato) {
            var pedido = contrato.getPedido();
            var automovel = pedido.getAutomovel();
            var cliente = pedido.getCliente();
            return new ContratoResponse(
                    contrato.getId(),
                    pedido.getId(),
                    cliente.getNome(),
                    cliente.getLogin(),
                    automovel.getId(),
                    automovel.getMatricula(),
                    automovel.getAno(),
                    automovel.getMarca(),
                    automovel.getModelo(),
                    automovel.getPlaca(),
                    pedido.getDataInicio(),
                    pedido.getDataFim(),
                    contrato.getDataFormalizacao(),
                    contrato.getAgenteLogin(),
                    contrato.getDetalhesCreditoBanco(),
                    pedido.getStatus());
        }
    }
}
