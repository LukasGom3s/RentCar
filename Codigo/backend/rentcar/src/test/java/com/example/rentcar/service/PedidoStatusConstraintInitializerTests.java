package com.example.rentcar.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class PedidoStatusConstraintInitializerTests {

    private static final String CONSTRAINT_QUERY = "SELECT pg_get_constraintdef(oid) "
            + "FROM pg_constraint "
            + "WHERE conname = ? AND conrelid = 'public.pedidos'::regclass";

    @Test
    void updatesOutdatedConstraintAtStartup() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(CONSTRAINT_QUERY, String.class, "pedidos_status_check"))
                .thenReturn(List.of("CHECK (status IN ('PENDENTE', 'APROVADO'))"));

        new PedidoStatusConstraintInitializer(jdbcTemplate).run(null);

        verify(jdbcTemplate).execute(anyString());
    }

    @Test
    void leavesConstraintAloneWhenAllStatusesAreAllowed() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(CONSTRAINT_QUERY, String.class, "pedidos_status_check"))
                .thenReturn(List.of("CHECK (status IN ('PENDENTE', 'EM_ANALISE', 'APROVADO', "
                        + "'AGUARDANDO_EXECUCAO', 'CONTRATO_EXECUTADO', 'REJEITADO', 'CANCELADO'))"));

        new PedidoStatusConstraintInitializer(jdbcTemplate).run(null);

        verify(jdbcTemplate, never()).execute(anyString());
    }
}
