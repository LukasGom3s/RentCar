package com.example.rentcar.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "contratos")
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @Column(nullable = false)
    private LocalDateTime dataFormalizacao;

    @Column(nullable = false)
    private String agenteLogin;

    @Column(length = 2000)
    private String detalhesCreditoBanco;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }

    public LocalDateTime getDataFormalizacao() { return dataFormalizacao; }
    public void setDataFormalizacao(LocalDateTime dataFormalizacao) { this.dataFormalizacao = dataFormalizacao; }

    public String getAgenteLogin() { return agenteLogin; }
    public void setAgenteLogin(String agenteLogin) { this.agenteLogin = agenteLogin; }

    public String getDetalhesCreditoBanco() { return detalhesCreditoBanco; }
    public void setDetalhesCreditoBanco(String detalhesCreditoBanco) {
        this.detalhesCreditoBanco = detalhesCreditoBanco;
    }
}
