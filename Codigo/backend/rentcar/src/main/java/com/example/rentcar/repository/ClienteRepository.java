package com.example.rentcar.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.rentcar.models.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    @EntityGraph(attributePaths = "entidadesEmpregadoras")
    Optional<Cliente> findByLogin(String login);
}
