package br.com.editalradar.coleta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "triagem")
public class Triagem {

    @Id
    @Column(name = "url_origem", length = 512)
    private String urlOrigem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoTriagem situacao;

    @Column(name = "verificado_em", nullable = false)
    private Instant verificadoEm;

    protected Triagem() {
    }

    public Triagem(String urlOrigem, SituacaoTriagem situacao, Instant verificadoEm) {
        this.urlOrigem = urlOrigem;
        this.situacao = situacao;
        this.verificadoEm = verificadoEm;
    }

    public String getUrlOrigem() {
        return urlOrigem;
    }

    public SituacaoTriagem getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoTriagem situacao) {
        this.situacao = situacao;
    }

    public Instant getVerificadoEm() {
        return verificadoEm;
    }

    public void setVerificadoEm(Instant verificadoEm) {
        this.verificadoEm = verificadoEm;
    }
}
