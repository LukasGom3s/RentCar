package com.example.rentcar.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.rentcar.models.Cliente;
import com.example.rentcar.models.Agente;
import com.example.rentcar.models.Administrador;
import com.example.rentcar.models.EntidadeEmpregadora;
import com.example.rentcar.models.TipoAgente;
import com.example.rentcar.repository.UsuarioRepository;

class UserServiceTests {

    @Test
    void assignsClientRoleToClientAccounts() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        Cliente client = new Cliente();
        client.setLogin("cliente@example.com");
        client.setPalavraPasse("encoded");
        when(repository.findClienteByLogin("cliente@example.com")).thenReturn(Optional.of(client));
        UserService service = new UserService(repository, mock(PasswordEncoder.class));

        var user = service.loadUserByUsername("cliente@example.com");

        assertTrue(user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CLIENTE")));
        assertFalse(user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_AGENTE")));
    }

    @Test
    void assignsAgentAndBankRolesToAgentAccounts() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        Agente agent = new Agente();
        agent.setLogin("agente@example.com");
        agent.setPalavraPasse("encoded");
        agent.setTipo(TipoAgente.BANCO);
        when(repository.findAgenteByLogin("agente@example.com")).thenReturn(Optional.of(agent));
        UserService service = new UserService(repository, mock(PasswordEncoder.class));

        var user = service.loadUserByUsername("agente@example.com");

        assertTrue(user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_AGENTE")));
        assertTrue(user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_AGENTE_BANCO")));
        assertFalse(user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CLIENTE")));
    }

    @Test
    void assignsAdministratorRoleToAdministratorAccounts() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        Administrador administrator = new Administrador();
        administrator.setLogin("admin@example.com");
        administrator.setPalavraPasse("encoded");
        when(repository.findAdministratorByLogin("admin@example.com")).thenReturn(Optional.of(administrator));
        UserService service = new UserService(repository, mock(PasswordEncoder.class));

        var user = service.loadUserByUsername("admin@example.com");

        assertTrue(user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void createsAgentWithEncodedPassword() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(repository.existsAccountByLogin("agent@example.com")).thenReturn(false);
        when(passwordEncoder.encode("a-strong-password")).thenReturn("encoded");
        when(repository.save(any(Agente.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserService service = new UserService(repository, passwordEncoder);

        Agente agent = service.createAgent(
                "agent@example.com",
                "a-strong-password",
                "Agent Name",
                "RentCar Motors",
                TipoAgente.EMPRESA);

        assertEquals("encoded", agent.getPalavraPasse());
        assertEquals(TipoAgente.EMPRESA, agent.getTipo());
        assertEquals("Agent Name", agent.getNome());
        assertEquals("RentCar Motors", agent.getOrganizacao());
        verify(repository).save(agent);
    }

    @Test
    void rejectsAgentPasswordBelowMinimumLength() {
        UserService service = new UserService(mock(UsuarioRepository.class), mock(PasswordEncoder.class));

        var exception = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> service.createAgent(
                        "agent@example.com",
                        "short",
                        "Agent Name",
                        "RentCar Motors",
                        TipoAgente.EMPRESA));

        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void acceptsEightCharacterAgentPassword() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(repository.existsAccountByLogin("agent@example.com")).thenReturn(false);
        when(passwordEncoder.encode("12345678")).thenReturn("encoded");
        when(repository.save(any(Agente.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserService service = new UserService(repository, passwordEncoder);

        Agente agent = service.createAgent(
                "agent@example.com",
                "12345678",
                "Agent Name",
                "RentCar Motors",
                TipoAgente.EMPRESA);

        assertEquals("encoded", agent.getPalavraPasse());
    }

    @Test
    void requiresAgentOrganization() {
        UserService service = new UserService(mock(UsuarioRepository.class), mock(PasswordEncoder.class));

        var exception = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> service.createAgent(
                        "agent@example.com",
                        "a-strong-password",
                        "Agent Name",
                        " ",
                        TipoAgente.EMPRESA));

        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void listsRegisteredAgents() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        Agente agent = new Agente();
        agent.setLogin("agent@example.com");
        when(repository.findAllAgents()).thenReturn(List.of(agent));
        UserService service = new UserService(repository, mock(PasswordEncoder.class));

        assertEquals(List.of(agent), service.getAgents());
    }

    @Test
    void returnsAnEmptyListWhenNoAgentsAreRegistered() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        when(repository.findAllAgents()).thenReturn(List.of());
        UserService service = new UserService(repository, mock(PasswordEncoder.class));

        assertTrue(service.getAgents().isEmpty());
    }

    @Test
    void updatesAgentProfileAndKeepsPasswordWhenNoNewPasswordIsProvided() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        Agente agent = new Agente();
        agent.setId(5L);
        agent.setLogin("agent@example.com");
        agent.setPalavraPasse("existing-encoded-password");
        agent.setNome("Old Name");
        agent.setTipo(TipoAgente.EMPRESA);
        when(repository.findAgentById(5L)).thenReturn(Optional.of(agent));
        when(repository.save(agent)).thenReturn(agent);
        UserService service = new UserService(repository, passwordEncoder);

        Agente updated = service.updateAgent(
                5L,
                "agent@example.com",
                "Updated Name",
                "Updated Company",
                TipoAgente.BANCO,
                "");

        assertEquals("Updated Name", updated.getNome());
        assertEquals("Updated Company", updated.getOrganizacao());
        assertEquals(TipoAgente.BANCO, updated.getTipo());
        assertEquals("existing-encoded-password", updated.getPalavraPasse());
        verify(repository).save(agent);
        verify(passwordEncoder, org.mockito.Mockito.never()).encode(any());
    }

    @Test
    void updatesAgentPasswordWhenANewPasswordIsProvided() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        Agente agent = new Agente();
        agent.setId(5L);
        agent.setLogin("agent@example.com");
        when(repository.findAgentById(5L)).thenReturn(Optional.of(agent));
        when(passwordEncoder.encode("new-strong-password")).thenReturn("encoded-new-password");
        when(repository.save(agent)).thenReturn(agent);
        UserService service = new UserService(repository, passwordEncoder);

        Agente updated = service.updateAgent(
                5L,
                "agent@example.com",
                "Agent",
                "Company",
                TipoAgente.EMPRESA,
                "new-strong-password");

        assertEquals("encoded-new-password", updated.getPalavraPasse());
    }

    @Test
    void synchronizesConfiguredPasswordForExistingAdministrator() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        Administrador administrator = new Administrador();
        administrator.setLogin("admin@example.com");
        when(repository.findAdministratorByLogin("admin@example.com"))
                .thenReturn(Optional.of(administrator));
        when(repository.existsAccountByLogin("admin@example.com")).thenReturn(true);
        when(passwordEncoder.encode("new-admin-password")).thenReturn("encoded-new-password");
        when(repository.save(any(Administrador.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserService service = new UserService(repository, passwordEncoder);

        Administrador updated = service.createAdministrator(
                "admin@example.com",
                "new-admin-password",
                "Administrator");

        assertEquals("encoded-new-password", updated.getPalavraPasse());
        assertEquals("Administrator", updated.getNome());
        verify(repository).save(administrator);
    }

    @Test
    void doesNotConvertExistingClientIntoAdministrator() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        when(repository.findAdministratorByLogin("client@example.com")).thenReturn(Optional.empty());
        when(repository.existsAccountByLogin("client@example.com")).thenReturn(true);
        UserService service = new UserService(repository, mock(PasswordEncoder.class));

        assertThrows(IllegalArgumentException.class, () -> service.createAdministrator(
                "client@example.com",
                "new-admin-password",
                "Administrator"));
    }

    @Test
    void registersEmployerIncomeWithTheClient() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(repository.existsAccountByLogin("cliente@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(repository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserService service = new UserService(repository, passwordEncoder);
        EntidadeEmpregadora employer = employer("Empresa", "2500.00");

        service.createUser(
                "cliente@example.com",
                "password",
                "Nome",
                "RG1",
                "123456",
                "Morada",
                "Profissão",
                List.of(employer));

        var captor = org.mockito.ArgumentCaptor.forClass(Cliente.class);
        verify(repository).save(captor.capture());
        Cliente saved = captor.getValue();
        assertEquals(1, saved.getEntidadesEmpregadoras().size());
        assertEquals(saved, saved.getEntidadesEmpregadoras().get(0).getCliente());
        assertEquals(new BigDecimal("2500.00"),
                saved.getEntidadesEmpregadoras().get(0).getRendimentoAuferido());
    }

    @Test
    void rejectsMoreThanThreeEmployerIncomes() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        when(repository.existsAccountByLogin("cliente@example.com")).thenReturn(false);
        UserService service = new UserService(repository, mock(PasswordEncoder.class));

        assertThrows(IllegalArgumentException.class, () -> service.createUser(
                "cliente@example.com",
                "password",
                "Nome",
                "RG1",
                "123456",
                "Morada",
                "Profissão",
                List.of(
                        employer("Empresa A", "1000"),
                        employer("Empresa B", "1200"),
                        employer("Empresa C", "1400"),
                        employer("Empresa D", "1600"))));
    }

    private EntidadeEmpregadora employer(String nome, String rendimento) {
        EntidadeEmpregadora entidade = new EntidadeEmpregadora();
        entidade.setNome(nome);
        entidade.setRendimentoAuferido(new BigDecimal(rendimento));
        return entidade;
    }
}
