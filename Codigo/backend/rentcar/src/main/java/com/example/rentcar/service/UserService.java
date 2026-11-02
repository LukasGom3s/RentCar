package com.example.rentcar.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.rentcar.models.Agente;
import com.example.rentcar.models.Administrador;
import com.example.rentcar.models.Cliente;
import com.example.rentcar.models.EntidadeEmpregadora;
import com.example.rentcar.models.TipoAgente;
import com.example.rentcar.models.Usuario;
import com.example.rentcar.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Método obrigatório do Spring Security para buscar o usuário no login
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = findUserByLogin(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        List<String> roles = new ArrayList<>(List.of("USER"));
        if (usuario instanceof Cliente) {
            roles.add("CLIENTE");
        } else if (usuario instanceof Agente agente) {
            roles.add("AGENTE");
            if (agente.getTipo() == TipoAgente.EMPRESA) {
                roles.add("AGENTE_EMPRESA");
            } else if (agente.getTipo() == TipoAgente.BANCO) {
                roles.add("AGENTE_BANCO");
            }
        } else if (usuario instanceof Administrador) {
            roles.add("ADMIN");
        } else {
            throw new UsernameNotFoundException("Tipo de usuário não suportado.");
        }

        return User.builder()
                .username(usuario.getLogin())
                .password(usuario.getPalavraPasse())
                .roles(roles.toArray(String[]::new))
                .build();
    }

    // 2. Criar um novo Cliente (salvando no Supabase)
    public void createUser(
            String email,
            String senha,
            String nome,
            String rg,
            String cpf,
            String morada,
            String profissao,
            List<EntidadeEmpregadora> entidadesEmpregadoras) {
        if (exists(email)) {
            throw new IllegalArgumentException("Email já cadastrado.");
        }
        validarEntidadesEmpregadoras(entidadesEmpregadoras);

        Cliente novoCliente = new Cliente();
        novoCliente.setLogin(email);
        novoCliente.setPalavraPasse(passwordEncoder.encode(senha));
        novoCliente.setNome(nome);
        novoCliente.setRg(rg);
        novoCliente.setCpf(cpf);
        novoCliente.setMorada(morada);
        novoCliente.setProfissao(profissao);
        entidadesEmpregadoras.forEach(entidade -> {
            entidade.setId(null);
            novoCliente.addEntidadeEmpregadora(entidade);
        });

        usuarioRepository.save(novoCliente);
    }

    private void validarEntidadesEmpregadoras(List<EntidadeEmpregadora> entidadesEmpregadoras) {
        if (entidadesEmpregadoras == null || entidadesEmpregadoras.size() > 3) {
            throw new IllegalArgumentException("Informe no máximo três entidades empregadoras.");
        }
        for (EntidadeEmpregadora entidade : entidadesEmpregadoras) {
            if (entidade == null
                    || entidade.getNome() == null
                    || entidade.getNome().isBlank()
                    || entidade.getRendimentoAuferido() == null
                    || entidade.getRendimentoAuferido().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Cada entidade empregadora deve ter nome e rendimento maior que zero.");
            }
            entidade.setNome(entidade.getNome().trim());
        }
    }

    // 3. Verificar se existe
    public boolean exists(String email) {
        return usuarioRepository.existsAccountByLogin(email);
    }

    private java.util.Optional<Usuario> findUserByLogin(String email) {
        return usuarioRepository.findClienteByLogin(email)
                .map(cliente -> (Usuario) cliente)
                .or(() -> usuarioRepository.findAgenteByLogin(email).map(agente -> (Usuario) agente))
                .or(() -> usuarioRepository.findAdministratorByLogin(email).map(administrador -> (Usuario) administrador));
    }

    public Agente createAgent(String email, String senha, String nome, String organizacao, TipoAgente tipo) {
        validarDadosAgente(email, nome, organizacao, tipo);
        if (senha == null || senha.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha deve ter pelo menos 8 caracteres.");
        }
        if (exists(email.trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado.");
        }

        Agente agente = new Agente();
        agente.setLogin(email.trim());
        agente.setPalavraPasse(passwordEncoder.encode(senha));
        agente.setNome(nome.trim());
        agente.setOrganizacao(organizacao.trim());
        agente.setTipo(tipo);
        return (Agente) usuarioRepository.save(agente);
    }

    public List<Agente> getAgents() {
        return usuarioRepository.findAllAgents();
    }

    public Agente getAgent(Long id) {
        return usuarioRepository.findAgentById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agente não encontrado."));
    }

    public Agente updateAgent(
            Long id,
            String email,
            String nome,
            String organizacao,
            TipoAgente tipo,
            String senha) {
        Agente agente = getAgent(id);
        validarDadosAgente(email, nome, organizacao, tipo);

        String normalizedEmail = email.trim();
        if (!normalizedEmail.equalsIgnoreCase(agente.getLogin())
                && exists(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado.");
        }
        if (senha != null && !senha.isBlank()) {
            if (senha.length() < 8) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A nova senha deve ter pelo menos 8 caracteres.");
            }
            agente.setPalavraPasse(passwordEncoder.encode(senha));
        }

        agente.setLogin(normalizedEmail);
        agente.setNome(nome.trim());
        agente.setOrganizacao(organizacao.trim());
        agente.setTipo(tipo);
        return (Agente) usuarioRepository.save(agente);
    }

    private void validarDadosAgente(String email, String nome, String organizacao, TipoAgente tipo) {
        if (email == null || email.isBlank() || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um e-mail válido.");
        }
        if (nome == null || nome.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome do agente é obrigatório.");
        }
        if (organizacao == null || organizacao.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe o banco ou a empresa do agente.");
        }
        if (tipo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione o tipo de agente.");
        }
    }

    public Administrador createAdministrator(String email, String senha, String nome) {
        var existingAdministrator = usuarioRepository.findAdministratorByLogin(email);
        if (usuarioRepository.existsAccountByLogin(email) && existingAdministrator.isEmpty()) {
            throw new IllegalArgumentException("O e-mail configurado para admin já pertence a outro usuário.");
        }

        Administrador administrador = existingAdministrator.orElseGet(Administrador::new);
        administrador.setLogin(email);
        administrador.setPalavraPasse(passwordEncoder.encode(senha));
        administrador.setNome(nome);
        return (Administrador) usuarioRepository.save(administrador);
    }

    // 4. Atualizar Senha
    public void updatePassword(String email, String novaSenha) {
        Usuario usuario = findUserByLogin(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

        usuario.setPalavraPasse(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }
}