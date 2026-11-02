package com.example.rentcar.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.rentcar.models.Contrato;
import com.example.rentcar.models.Pedido;
import com.example.rentcar.models.StatusPedido;
import com.example.rentcar.repository.ContratoRepository;
import com.example.rentcar.repository.PedidoRepository;

class ContratoServiceTests {

    @Test
    void formalizesContractForAnOrderAwaitingExecution() {
        ContratoRepository contratoRepository = mock(ContratoRepository.class);
        PedidoRepository pedidoRepository = mock(PedidoRepository.class);
        Pedido pedido = pendingPedido(StatusPedido.AGUARDANDO_EXECUCAO);
        when(pedidoRepository.findDistinctById(7L)).thenReturn(Optional.of(pedido));
        when(contratoRepository.existsByPedido_Id(7L)).thenReturn(false);
        when(contratoRepository.save(any(Contrato.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ContratoService service = new ContratoService(contratoRepository, pedidoRepository);

        Contrato contrato = service.execute(7L, "banco@example.com", true, "Crédito aprovado.");

        assertEquals(pedido, contrato.getPedido());
        assertEquals("banco@example.com", contrato.getAgenteLogin());
        assertEquals("Crédito aprovado.", contrato.getDetalhesCreditoBanco());
        assertEquals(StatusPedido.CONTRATO_EXECUTADO, pedido.getStatus());
        verify(contratoRepository).save(contrato);
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void formalizesBasicContractWithoutCredit() {
        ContratoRepository contratoRepository = mock(ContratoRepository.class);
        PedidoRepository pedidoRepository = mock(PedidoRepository.class);
        Pedido pedido = pendingPedido(StatusPedido.AGUARDANDO_EXECUCAO);
        when(pedidoRepository.findDistinctById(7L)).thenReturn(Optional.of(pedido));
        when(contratoRepository.existsByPedido_Id(7L)).thenReturn(false);
        when(contratoRepository.save(any(Contrato.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ContratoService service = new ContratoService(contratoRepository, pedidoRepository);

        Contrato contrato = service.execute(7L, "empresa@example.com", false, null);

        assertNull(contrato.getDetalhesCreditoBanco());
        assertEquals(StatusPedido.CONTRATO_EXECUTADO, pedido.getStatus());
    }

    @Test
    void onlyBankAgentCanRegisterBankCreditDetails() {
        ContratoRepository contratoRepository = mock(ContratoRepository.class);
        PedidoRepository pedidoRepository = mock(PedidoRepository.class);
        ContratoService service = new ContratoService(contratoRepository, pedidoRepository);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.execute(7L, "empresa@example.com", false, "Crédito"));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(pedidoRepository, never()).findDistinctById(7L);
    }

    @Test
    void cannotFormalizeContractBeforeOrderIsForwarded() {
        ContratoRepository contratoRepository = mock(ContratoRepository.class);
        PedidoRepository pedidoRepository = mock(PedidoRepository.class);
        when(pedidoRepository.findDistinctById(7L))
                .thenReturn(Optional.of(pendingPedido(StatusPedido.APROVADO)));
        ContratoService service = new ContratoService(contratoRepository, pedidoRepository);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.execute(7L, "banco@example.com", true, null));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(contratoRepository, never()).save(any(Contrato.class));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void cannotCreateAnotherContractForTheSameOrder() {
        ContratoRepository contratoRepository = mock(ContratoRepository.class);
        PedidoRepository pedidoRepository = mock(PedidoRepository.class);
        when(pedidoRepository.findDistinctById(7L))
                .thenReturn(Optional.of(pendingPedido(StatusPedido.AGUARDANDO_EXECUCAO)));
        when(contratoRepository.existsByPedido_Id(7L)).thenReturn(true);
        ContratoService service = new ContratoService(contratoRepository, pedidoRepository);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.execute(7L, "banco@example.com", true, null));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(contratoRepository, never()).save(any(Contrato.class));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void rejectsCreditDetailsLongerThanTwoThousandCharacters() {
        ContratoRepository contratoRepository = mock(ContratoRepository.class);
        PedidoRepository pedidoRepository = mock(PedidoRepository.class);
        ContratoService service = new ContratoService(contratoRepository, pedidoRepository);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.execute(7L, "banco@example.com", true, "x".repeat(2001)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(pedidoRepository, never()).findDistinctById(7L);
    }

    private Pedido pendingPedido(StatusPedido status) {
        Pedido pedido = new Pedido();
        pedido.setId(7L);
        pedido.setStatus(status);
        return pedido;
    }
}
