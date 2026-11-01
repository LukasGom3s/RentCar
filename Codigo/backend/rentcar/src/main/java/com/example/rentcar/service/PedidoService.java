package com.example.rentcar.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.rentcar.models.Automovel;
import com.example.rentcar.models.Cliente;
import com.example.rentcar.models.Pedido;
import com.example.rentcar.models.StatusPedido;
import com.example.rentcar.repository.AutomovelRepository;
import com.example.rentcar.repository.ClienteRepository;
import com.example.rentcar.repository.PedidoRepository;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final AutomovelRepository automovelRepository;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ClienteRepository clienteRepository,
            AutomovelRepository automovelRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.automovelRepository = automovelRepository;
    }

    @Transactional(readOnly = true)
    public List<Pedido> findMyPedidos(String login) {
        return pedidoRepository.findByCliente_LoginOrderByDataCriacaoDesc(login);
    }

    @Transactional
    public Pedido create(String login, Long automovelId, LocalDate dataInicio, LocalDate dataFim) {
        if (automovelId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione um automóvel.");
        }
        if (dataInicio == null || dataFim == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe as datas do aluguel.");
        }
        if (dataInicio.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data de início não pode estar no passado.");
        }
        if (!dataFim.isAfter(dataInicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data final deve ser posterior à data inicial.");
        }

        Cliente cliente = clienteRepository.findByLogin(login)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Perfil de cliente não encontrado."));
        Automovel automovel = automovelRepository.findById(automovelId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Automóvel não encontrado."));

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setAutomovel(automovel);
        pedido.setDataCriacao(LocalDateTime.now());
        pedido.setDataInicio(dataInicio);
        pedido.setDataFim(dataFim);
        pedido.setStatus(StatusPedido.EM_ANALISE);
        return pedidoRepository.save(pedido);
    }
}
