package com.example.rentcar.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.rentcar.models.Cliente;
import com.example.rentcar.repository.ClienteRepository;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
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
            String profissao) {
        requireValue(nome, "Nome");
        requireValue(rg, "RG");
        requireValue(cpf, "CPF");
        requireValue(morada, "Morada");
        requireValue(profissao, "Profissão");

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
        return clienteRepository.save(cliente);
    }

    @Transactional
    public void delete(String login) {
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
}
