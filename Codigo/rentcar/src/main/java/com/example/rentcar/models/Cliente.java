package com.example.rentcar.models;

import jakarta.persistence.Entity;

@Entity
public class Cliente extends Usuario {
    
    private String rg;
    private String cpf;
    private String nome;
    private String morada;
    private String profissao;

    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getMorada() { return morada; }
    public void setMorada(String morada) { this.morada = morada; }

    public String getProfissao() { return profissao; }
    public void setProfissao(String profissao) { this.profissao = profissao; }
}