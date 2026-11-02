package com.example.rentcar.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.rentcar.models.Contrato;
import com.example.rentcar.models.Pedido;
import com.example.rentcar.models.StatusPedido;
import com.example.rentcar.repository.ContratoRepository;
import com.example.rentcar.repository.PedidoRepository;

@Service
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final PedidoRepository pedidoRepository;

    public ContratoService(ContratoRepository contratoRepository, PedidoRepository pedidoRepository) {
        this.contratoRepository = contratoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public List<Contrato> findAll() {
        return contratoRepository.findAllByOrderByDataFormalizacaoDesc();
    }

    @Transactional(readOnly = true)
    public List<Contrato> findMyContracts(String login) {
        return contratoRepository.findByPedido_Cliente_LoginOrderByDataFormalizacaoDesc(login);
    }

    @Transactional
    public Contrato execute(
            Long pedidoId,
            String agenteLogin,
            boolean agenteBanco,
            String detalhesCreditoBanco) {
        String creditoNormalizado = normalizeCreditDetails(detalhesCreditoBanco, agenteBanco);

        Pedido pedido = pedidoRepository.findDistinctById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido não encontrado."));
        if (pedido.getStatus() != StatusPedido.AGUARDANDO_EXECUCAO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Somente pedidos encaminhados para execução podem ser formalizados.");
        }
        if (contratoRepository.existsByPedido_Id(pedidoId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este pedido já possui um contrato formalizado.");
        }

        Contrato contrato = new Contrato();
        contrato.setPedido(pedido);
        contrato.setDataFormalizacao(LocalDateTime.now());
        contrato.setAgenteLogin(agenteLogin);
        contrato.setDetalhesCreditoBanco(creditoNormalizado);

        Contrato saved = contratoRepository.save(contrato);
        pedido.setStatus(StatusPedido.CONTRATO_EXECUTADO);
        pedidoRepository.save(pedido);
        return saved;
    }

    private String normalizeCreditDetails(String details, boolean agenteBanco) {
        if (details == null || details.isBlank()) {
            return null;
        }
        if (!agenteBanco) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Somente um agente de banco pode registrar os detalhes do crédito bancário.");
        }

        String normalized = details.trim();
        if (normalized.length() > 2000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Os detalhes do crédito devem ter no máximo 2000 caracteres.");
        }
        return normalized;
    }
}
