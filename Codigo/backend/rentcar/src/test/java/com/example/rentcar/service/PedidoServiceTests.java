package com.example.rentcar.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.http.HttpStatus;
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

    @Test
    void updatesOwnedOrderAndReturnsApprovedOrdersToAnalysis() {
        PedidoRepository pedidoRepository = mock(PedidoRepository.class);
        AutomovelRepository automovelRepository = mock(AutomovelRepository.class);
        Pedido pedido = pendingPedido(StatusPedido.APROVADO);
        Automovel replacement = new Automovel();
        replacement.setId(9L);
        when(pedidoRepository.findByIdAndCliente_Login(4L, "cliente@example.com"))
                .thenReturn(Optional.of(pedido));
        when(automovelRepository.findById(9L)).thenReturn(Optional.of(replacement));
        when(pedidoRepository.save(pedido)).thenReturn(pedido);
        PedidoService service = new PedidoService(
                pedidoRepository,
                mock(ClienteRepository.class),
                automovelRepository);
        LocalDate inicio = LocalDate.now().plusDays(5);

        Pedido updated = service.update("cliente@example.com", 4L, 9L, inicio, inicio.plusDays(2));

        assertEquals(replacement, updated.getAutomovel());
        assertEquals(inicio, updated.getDataInicio());
        assertEquals(inicio.plusDays(2), updated.getDataFim());
        assertEquals(StatusPedido.EM_ANALISE, updated.getStatus());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void cancelsOwnedOrderInAnalysis() {
        PedidoRepository pedidoRepository = mock(PedidoRepository.class);
        Pedido pedido = pendingPedido(StatusPedido.EM_ANALISE);
        when(pedidoRepository.findByIdAndCliente_Login(4L, "cliente@example.com"))
                .thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(pedido)).thenReturn(pedido);
        PedidoService service = new PedidoService(
                pedidoRepository,
                mock(ClienteRepository.class),
                mock(AutomovelRepository.class));

        Pedido cancelled = service.cancel("cliente@example.com", 4L);

        assertEquals(StatusPedido.CANCELADO, cancelled.getStatus());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void rejectsModificationOfRejectedOrder() {
        PedidoRepository pedidoRepository = mock(PedidoRepository.class);
        Pedido pedido = pendingPedido(StatusPedido.REJEITADO);
        when(pedidoRepository.findByIdAndCliente_Login(4L, "cliente@example.com"))
                .thenReturn(Optional.of(pedido));
        PedidoService service = new PedidoService(
                pedidoRepository,
                mock(ClienteRepository.class),
                mock(AutomovelRepository.class));
        LocalDate inicio = LocalDate.now().plusDays(5);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.update(
                "cliente@example.com",
                4L,
                9L,
                inicio,
                inicio.plusDays(2)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void doesNotAllowUpdatingAnotherClientsOrder() {
        PedidoRepository pedidoRepository = mock(PedidoRepository.class);
        when(pedidoRepository.findByIdAndCliente_Login(eq(4L), eq("cliente@example.com")))
                .thenReturn(Optional.empty());
        PedidoService service = new PedidoService(
                pedidoRepository,
                mock(ClienteRepository.class),
                mock(AutomovelRepository.class));
        LocalDate inicio = LocalDate.now().plusDays(5);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.update(
                "cliente@example.com",
                4L,
                9L,
                inicio,
                inicio.plusDays(2)));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    private Pedido pendingPedido(StatusPedido status) {
        Pedido pedido = new Pedido();
        pedido.setId(4L);
        pedido.setStatus(status);
        pedido.setAutomovel(new Automovel());
        return pedido;
    }
}
