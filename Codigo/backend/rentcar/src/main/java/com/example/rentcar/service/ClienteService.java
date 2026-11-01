package com.example.rentcar.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.rentcar.models.Cliente;
import com.example.rentcar.models.EntidadeEmpregadora;
import com.example.rentcar.repository.ClienteRepository;
import com.example.rentcar.repository.PedidoRepository;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;

    public ClienteService(ClienteRepository clienteRepository, PedidoRepository pedidoRepository) {
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public Cliente getByLogin(String login) {
        return clienteRepository.findByLogin(login)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Perfil de cliente não encontrado."));
    }

    @Transactional
    public Cliente update(
            String login,
            String nome,
            String rg,
            String cpf,
            String morada,
            String profissao,
            List<EntidadeEmpregadora> entidadesEmpregadoras) {
        requireValue(nome, "Nome");
        requireValue(rg, "RG");
        requireValue(cpf, "CPF");
        requireValue(morada, "Morada");
        requireValue(profissao, "Profissão");
        validateEntidadesEmpregadoras(entidadesEmpregadoras);

        if (!cpf.matches("\\d+")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O CPF deve conter somente números.");
        }

        if (!rg.matches("[a-zA-Z0-9]+")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O RG deve conter somente letras e números.");
        }

        Cliente cliente = getByLogin(login);
        cliente.setNome(nome.trim());
        cliente.setRg(rg.trim());
        cliente.setCpf(cpf.trim());
        cliente.setMorada(morada.trim());
        cliente.setProfissao(profissao.trim());
        cliente.setEntidadesEmpregadoras(entidadesEmpregadoras);
        return clienteRepository.save(cliente);
    }

    @Transactional
    public void delete(String login) {
        if (pedidoRepository.existsByCliente_Login(login)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é possível excluir a conta enquanto houver pedidos de aluguel registrados.");
        }
        Cliente cliente = getByLogin(login);
        clienteRepository.delete(cliente);
    }

    private void requireValue(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    field + " é obrigatório.");
        }
    }

    private void validateEntidadesEmpregadoras(List<EntidadeEmpregadora> entidadesEmpregadoras) {
        if (entidadesEmpregadoras == null || entidadesEmpregadoras.size() > 3) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe no máximo três entidades empregadoras.");
        }
        for (EntidadeEmpregadora entidade : entidadesEmpregadoras) {
            if (entidade == null
                    || entidade.getNome() == null
                    || entidade.getNome().isBlank()
                    || entidade.getRendimentoAuferido() == null
                    || entidade.getRendimentoAuferido().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cada entidade empregadora deve ter nome e rendimento maior que zero.");
            }
            entidade.setNome(entidade.getNome().trim());
        }
    }
}
