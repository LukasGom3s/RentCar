package com.example.rentcar.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.rentcar.models.Contrato;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    boolean existsByPedido_Id(Long pedidoId);

    @EntityGraph(attributePaths = {"pedido", "pedido.cliente", "pedido.automovel"})
    List<Contrato> findAllByOrderByDataFormalizacaoDesc();

    @EntityGraph(attributePaths = {"pedido", "pedido.cliente", "pedido.automovel"})
    List<Contrato> findByPedido_Cliente_LoginOrderByDataFormalizacaoDesc(String login);
}
