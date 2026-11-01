package com.example.rentcar.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.example.rentcar.models.Automovel;
import com.example.rentcar.models.Cliente;
import com.example.rentcar.models.Pedido;
import com.example.rentcar.models.StatusPedido;
import com.example.rentcar.repository.AutomovelRepository;
import com.example.rentcar.repository.ClienteRepository;
import com.example.rentcar.repository.PedidoRepository;

class PedidoServiceTests {

    @Test
    void createsAnAnalysisOrderForTheAuthenticatedClientAndRegisteredCar() {
        PedidoRepository pedidoRepository = mock(PedidoRepository.class);
        ClienteRepository clienteRepository = mock(ClienteRepository.class);
        AutomovelRepository automovelRepository = mock(AutomovelRepository.class);
        Cliente cliente = new Cliente();
        cliente.setLogin("cliente@example.com");
        Automovel automovel = new Automovel();
        automovel.setId(8L);
        automovel.setMarca("Toyota");
        when(clienteRepository.findByLogin("cliente@example.com")).thenReturn(Optional.of(cliente));
        when(automovelRepository.findById(8L)).thenReturn(Optional.of(automovel));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PedidoService service = new PedidoService(pedidoRepository, clienteRepository, automovelRepository);

        LocalDate inicio = LocalDate.now().plusDays(2);
        Pedido pedido = service.create("cliente@example.com", 8L, inicio, inicio.plusDays(3));

        assertEquals(cliente, pedido.getCliente());
        assertEquals(automovel, pedido.getAutomovel());
        assertEquals(inicio, pedido.getDataInicio());
        assertEquals(inicio.plusDays(3), pedido.getDataFim());
        assertEquals(StatusPedido.EM_ANALISE, pedido.getStatus());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void rejectsEndDateBeforeOrEqualToStartDate() {
        PedidoRepository pedidoRepository = mock(PedidoRepository.class);
        PedidoService service = new PedidoService(
                pedidoRepository,
                mock(ClienteRepository.class),
                mock(AutomovelRepository.class));
        LocalDate inicio = LocalDate.now().plusDays(2);

        assertThrows(ResponseStatusException.class, () -> service.create(
                "cliente@example.com",
                8L,
                inicio,
                inicio));

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }
}
