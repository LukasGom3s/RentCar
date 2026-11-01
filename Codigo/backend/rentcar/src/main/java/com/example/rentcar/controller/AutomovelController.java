package com.example.rentcar.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.rentcar.models.Automovel;
import com.example.rentcar.repository.AutomovelRepository;

@RestController
@RequestMapping("/api/automoveis")
public class AutomovelController {

    private final AutomovelRepository automovelRepository;

    public AutomovelController(AutomovelRepository automovelRepository) {
        this.automovelRepository = automovelRepository;
    }

    @GetMapping
    public List<AutomovelResponse> findAll() {
        return automovelRepository.findAll().stream()
                .map(AutomovelResponse::from)
                .toList();
    }

    public record AutomovelResponse(
            Long id,
            String matricula,
            int ano,
            String marca,
            String modelo,
            String placa) {
        static AutomovelResponse from(Automovel automovel) {
            return new AutomovelResponse(
                    automovel.getId(),
                    automovel.getMatricula(),
                    automovel.getAno(),
                    automovel.getMarca(),
                    automovel.getModelo(),
                    automovel.getPlaca());
        }
    }
}
