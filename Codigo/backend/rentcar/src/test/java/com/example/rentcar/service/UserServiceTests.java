package com.example.rentcar.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.example.rentcar.models.EntidadeEmpregadora;
import com.example.rentcar.repository.UsuarioRepository;

class UserServiceTests {

    @Test
    void registersEmployerIncomeWithTheClient() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(repository.findByLogin("cliente@example.com")).thenReturn(Optional.empty());
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
        when(repository.findByLogin("cliente@example.com")).thenReturn(Optional.empty());
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
