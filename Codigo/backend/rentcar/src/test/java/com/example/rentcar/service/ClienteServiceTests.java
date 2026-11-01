package com.example.rentcar.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.example.rentcar.models.Cliente;
import com.example.rentcar.repository.ClienteRepository;
import com.example.rentcar.repository.PedidoRepository;

class ClienteServiceTests {

    @Test
    void updatesOnlyProfileFieldsForAuthenticatedLogin() {
        ClienteRepository repository = mock(ClienteRepository.class);
        PedidoRepository pedidoRepository = mock(PedidoRepository.class);
        Cliente existing = new Cliente();
        existing.setLogin("cliente@example.com");
        existing.setNome("Nome antigo");
        existing.setCpf("123");
        existing.setRg("RG1");
        when(repository.findByLogin("cliente@example.com")).thenReturn(Optional.of(existing));
        when(repository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ClienteService service = new ClienteService(repository, pedidoRepository);

        Cliente updated = service.update(
                "cliente@example.com",
                "Nome novo",
                "RG2",
                "456",
                "Morada nova",
                "Profissão nova");

        assertEquals("Nome novo", updated.getNome());
        assertEquals("456", updated.getCpf());
        assertEquals("RG2", updated.getRg());
        assertEquals("Morada nova", updated.getMorada());
        assertEquals("Profissão nova", updated.getProfissao());
        assertEquals("cliente@example.com", updated.getLogin());
    }

    @Test
    void rejectsInvalidCpfWithoutLoadingClient() {
        ClienteRepository repository = mock(ClienteRepository.class);
        PedidoRepository pedidoRepository = mock(PedidoRepository.class);
        ClienteService service = new ClienteService(repository, pedidoRepository);

        assertThrows(ResponseStatusException.class, () -> service.update(
                "cliente@example.com",
                "Nome",
                "RG1",
                "12-34",
                "Morada",
                "Profissão"));

        verify(repository, never()).findByLogin(any());
    }

    @Test
    void deletesTheClientResolvedFromAuthenticatedLogin() {
        ClienteRepository repository = mock(ClienteRepository.class);
        PedidoRepository pedidoRepository = mock(PedidoRepository.class);
        Cliente cliente = new Cliente();
        cliente.setLogin("cliente@example.com");
        when(repository.findByLogin("cliente@example.com")).thenReturn(Optional.of(cliente));
        ClienteService service = new ClienteService(repository, pedidoRepository);

        service.delete("cliente@example.com");

        verify(repository).delete(cliente);
    }
}
