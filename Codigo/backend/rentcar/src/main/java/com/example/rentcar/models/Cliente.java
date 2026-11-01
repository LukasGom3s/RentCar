package com.example.rentcar.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

@Entity
public class Cliente extends Usuario {
    
    private String rg;
    private String cpf;
    private String nome;
    private String morada;
    private String profissao;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EntidadeEmpregadora> entidadesEmpregadoras = new ArrayList<>();

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

    public List<EntidadeEmpregadora> getEntidadesEmpregadoras() { return entidadesEmpregadoras; }
    public void setEntidadesEmpregadoras(List<EntidadeEmpregadora> entidadesEmpregadoras) {
        this.entidadesEmpregadoras.clear();
        if (entidadesEmpregadoras != null) {
            entidadesEmpregadoras.forEach(this::addEntidadeEmpregadora);
        }
    }

    public void addEntidadeEmpregadora(EntidadeEmpregadora entidadeEmpregadora) {
        entidadesEmpregadoras.add(entidadeEmpregadora);
        entidadeEmpregadora.setCliente(this);
    }
}