package com.example.rentcar.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.rentcar.models.Pedido;
import com.example.rentcar.models.StatusPedido;
import com.example.rentcar.service.PedidoService;

@RestController
@RequestMapping("/api/agente/pedidos")
public class AgentPedidoController {

    private final PedidoService pedidoService;

    public AgentPedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<ReviewPedidoResponse> findPedidosAwaitingReview() {
        return pedidoService.findPedidosAwaitingFinancialReview().stream()
                .map(ReviewPedidoResponse::from)
                .toList();
    }

    @PatchMapping("/{pedidoId}/parecer")
    public ReviewResultResponse evaluate(
            @PathVariable Long pedidoId,
            @RequestBody FinancialOpinionRequest request) {
        Pedido pedido = pedidoService.evaluateFinancially(
                pedidoId,
                request == null ? null : request.aprovado());
        return new ReviewResultResponse(pedido.getId(), pedido.getStatus());
    }

    public record FinancialOpinionRequest(Boolean aprovado) {
    }

    public record ReviewResultResponse(Long id, StatusPedido status) {
    }

    public record ReviewPedidoResponse(
            Long id,
            String clienteNome,
            String clienteProfissao,
            List<RendimentoResponse> rendimentos,
            Long automovelId,
            String matricula,
            int ano,
            String marca,
            String modelo,
            String placa,
            LocalDate dataInicio,
            LocalDate dataFim,
            LocalDateTime dataCriacao,
            StatusPedido status) {
        static ReviewPedidoResponse from(Pedido pedido) {
            var cliente = pedido.getCliente();
            var automovel = pedido.getAutomovel();
            return new ReviewPedidoResponse(
                    pedido.getId(),
                    cliente.getNome(),
                    cliente.getProfissao(),
                    cliente.getEntidadesEmpregadoras().stream()
                            .map(entidade -> new RendimentoResponse(
                                    entidade.getNome(),
                                    entidade.getRendimentoAuferido()))
                            .toList(),
                    automovel.getId(),
                    automovel.getMatricula(),
                    automovel.getAno(),
                    automovel.getMarca(),
                    automovel.getModelo(),
                    automovel.getPlaca(),
                    pedido.getDataInicio(),
                    pedido.getDataFim(),
                    pedido.getDataCriacao(),
                    pedido.getStatus());
        }
    }

    public record RendimentoResponse(String entidadeEmpregadora, BigDecimal valor) {
    }
}
