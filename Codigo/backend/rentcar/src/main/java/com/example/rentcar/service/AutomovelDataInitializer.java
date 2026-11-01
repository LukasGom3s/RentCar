package com.example.rentcar.service;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.rentcar.models.Automovel;
import com.example.rentcar.repository.AutomovelRepository;

@Component
public class AutomovelDataInitializer implements ApplicationRunner {

    private final AutomovelRepository automovelRepository;

    public AutomovelDataInitializer(AutomovelRepository automovelRepository) {
        this.automovelRepository = automovelRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (automovelRepository.count() > 0) {
            return;
        }

        automovelRepository.saveAll(List.of(
                createAutomovel("RENT-001", 2024, "Toyota", "Corolla", "RNT1A24"),
                createAutomovel("RENT-002", 2023, "Chevrolet", "Onix", "RNT2B23"),
                createAutomovel("RENT-003", 2024, "Hyundai", "HB20", "RNT3C24")));
    }

    private Automovel createAutomovel(String matricula, int ano, String marca, String modelo, String placa) {
        Automovel automovel = new Automovel();
        automovel.setMatricula(matricula);
        automovel.setAno(ano);
        automovel.setMarca(marca);
        automovel.setModelo(modelo);
        automovel.setPlaca(placa);
        return automovel;
    }
}
