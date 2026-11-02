package com.example.rentcar.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.example.rentcar.models.Agente;
import com.example.rentcar.models.TipoAgente;
import com.example.rentcar.service.UserService;

@RestController
@RequestMapping("/api/admin/agentes")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<AgentResponse> getAgents() {
        return userService.getAgents().stream()
                .map(AgentResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public AgentResponse getAgent(@PathVariable Long id) {
        return AgentResponse.from(userService.getAgent(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgentResponse createAgent(
            @RequestBody CreateAgentRequest request) {
        Agente agente = userService.createAgent(
                request.email(),
                request.senha(),
                request.nome(),
                request.organizacao(),
                request.tipo());
        return AgentResponse.from(agente);
    }

    @PutMapping("/{id}")
    public AgentResponse updateAgent(
            @PathVariable Long id,
            @RequestBody UpdateAgentRequest request) {
        Agente agente = userService.updateAgent(
                id,
                request.email(),
                request.nome(),
                request.organizacao(),
                request.tipo(),
                request.senha());
        return AgentResponse.from(agente);
    }

    public record CreateAgentRequest(
            String email,
            String senha,
            String nome,
            String organizacao,
            TipoAgente tipo) {
    }

    public record UpdateAgentRequest(
            String email,
            String senha,
            String nome,
            String organizacao,
            TipoAgente tipo) {
    }

    public record AgentResponse(Long id, String email, String nome, String organizacao, TipoAgente tipo) {
        static AgentResponse from(Agente agente) {
            return new AgentResponse(
                    agente.getId(),
                    agente.getLogin(),
                    agente.getNome(),
                    agente.getOrganizacao(),
                    agente.getTipo());
        }
    }
}
