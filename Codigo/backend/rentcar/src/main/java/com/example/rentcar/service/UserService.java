package com.example.rentcar.service;

import com.example.rentcar.models.Cliente;
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
        Usuario usuario = usuarioRepository.findByLogin(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        return User.builder()
                .username(usuario.getLogin())
                .password(usuario.getPalavraPasse())
                .roles("USER") // O ideal futuramente é separar ROLE_CLIENTE e ROLE_AGENTE
                .build();
    }

    // 2. Criar um novo Cliente (salvando no Supabase)
    public void createUser(String email, String senha, String nome) {
        if (exists(email)) {
            throw new IllegalArgumentException("Email já cadastrado.");
        }

        Cliente novoCliente = new Cliente();
        novoCliente.setLogin(email);
        novoCliente.setPalavraPasse(passwordEncoder.encode(senha));
        novoCliente.setNome(nome);
        
        usuarioRepository.save(novoCliente);
    }

    // 3. Verificar se existe
    public boolean exists(String email) {
        return usuarioRepository.findByLogin(email).isPresent();
    }

    // 4. Atualizar Senha
    public void updatePassword(String email, String novaSenha) {
        Usuario usuario = usuarioRepository.findByLogin(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

        usuario.setPalavraPasse(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }
}