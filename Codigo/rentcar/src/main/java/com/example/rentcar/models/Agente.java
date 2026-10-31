package com.example.rentcar.models;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class Agente extends Usuario {
    
    private String nome;

    @Enumerated(EnumType.STRING)
    private TipoAgente tipo;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public TipoAgente getTipo() { return tipo; }
    public void setTipo(TipoAgente tipo) { this.tipo = tipo; }
}