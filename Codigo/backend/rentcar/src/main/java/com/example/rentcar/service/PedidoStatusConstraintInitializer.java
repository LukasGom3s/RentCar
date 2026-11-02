package com.example.rentcar.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PedidoStatusConstraintInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(PedidoStatusConstraintInitializer.class);
    private static final String CONSTRAINT_NAME = "pedidos_status_check";
    private static final List<String> ALLOWED_STATUSES = List.of(
            "PENDENTE",
            "EM_ANALISE",
            "APROVADO",
            "AGUARDANDO_EXECUCAO",
            "CONTRATO_EXECUTADO",
            "REJEITADO",
            "CANCELADO");

    private final JdbcTemplate jdbcTemplate;

    public PedidoStatusConstraintInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> definitions = jdbcTemplate.queryForList(
                "SELECT pg_get_constraintdef(oid) "
                        + "FROM pg_constraint "
                        + "WHERE conname = ? AND conrelid = 'public.pedidos'::regclass",
                String.class,
                CONSTRAINT_NAME);

        if (!definitions.isEmpty() && allowsAllStatuses(definitions.get(0))) {
            return;
        }

        String allowedValues = ALLOWED_STATUSES.stream()
                .map(status -> "'" + status + "'")
                .collect(java.util.stream.Collectors.joining(", "));
        jdbcTemplate.execute(
                "ALTER TABLE public.pedidos "
                        + "DROP CONSTRAINT IF EXISTS " + CONSTRAINT_NAME + ", "
                        + "ADD CONSTRAINT " + CONSTRAINT_NAME
                        + " CHECK (status IN (" + allowedValues + "))");
        logger.info("Updated the pedidos status constraint to include all supported statuses.");
    }

    private boolean allowsAllStatuses(String definition) {
        return ALLOWED_STATUSES.stream()
                .allMatch(status -> definition.contains("'" + status + "'"));
    }
}
