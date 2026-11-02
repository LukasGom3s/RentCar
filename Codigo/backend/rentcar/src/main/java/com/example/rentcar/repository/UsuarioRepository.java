package com.example.rentcar.repository;

import com.example.rentcar.models.Administrador;
import com.example.rentcar.models.Agente;
import com.example.rentcar.models.Cliente;
import com.example.rentcar.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    @Query("select cliente from Cliente cliente where cliente.login = :login")
    Optional<Cliente> findClienteByLogin(@Param("login") String login);

    @Query("select agente from Agente agente where agente.login = :login")
    Optional<Agente> findAgenteByLogin(@Param("login") String login);

    @Query("select agente from Agente agente order by agente.nome asc, agente.login asc")
    List<Agente> findAllAgents();

    @Query("select agente from Agente agente where agente.id = :id")
    Optional<Agente> findAgentById(@Param("id") Long id);

    @Query("select administrador from Administrador administrador where administrador.login = :login")
    Optional<Administrador> findAdministratorByLogin(@Param("login") String login);

    @Query(value = "select exists (select 1 from usuario where login = :login)", nativeQuery = true)
    boolean existsAccountByLogin(@Param("login") String login);
}