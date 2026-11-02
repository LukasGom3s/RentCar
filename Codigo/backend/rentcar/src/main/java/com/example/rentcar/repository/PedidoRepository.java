package com.example.rentcar.repository;

import java.util.List;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.rentcar.models.Pedido;
import com.example.rentcar.models.StatusPedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    @EntityGraph(attributePaths = "automovel")
    List<Pedido> findByCliente_LoginOrderByDataCriacaoDesc(String login);

    @EntityGraph(attributePaths = {"automovel", "cliente", "cliente.entidadesEmpregadoras"})
    List<Pedido> findDistinctByStatusOrderByDataCriacaoAsc(StatusPedido status);

    @EntityGraph(attributePaths = "automovel")
    Optional<Pedido> findByIdAndCliente_Login(Long id, String login);

    boolean existsByCliente_Login(String login);
}
