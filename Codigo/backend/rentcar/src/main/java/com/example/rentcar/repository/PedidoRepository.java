package com.example.rentcar.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.rentcar.models.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    @EntityGraph(attributePaths = "automovel")
    List<Pedido> findByCliente_LoginOrderByDataCriacaoDesc(String login);

    boolean existsByCliente_Login(String login);
}
