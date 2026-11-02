package com.example.rentcar.models;

import jakarta.persistence.Entity;

@Entity
public class Administrador extends Usuario {

    private String nome;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
