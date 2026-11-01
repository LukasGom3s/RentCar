package com.example.rentcar.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.rentcar.models.Automovel;

public interface AutomovelRepository extends JpaRepository<Automovel, Long> {
}
