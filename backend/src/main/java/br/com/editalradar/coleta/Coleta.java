package br.com.editalradar.coleta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "coleta")
public class Coleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iniciada_em", nullable = false)
    private Instant iniciadaEm;

    @Column(name = "finalizada_em")
    private Instant finalizadaEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusColeta status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigemColeta origem;

    @Column(name = "total_listagem", nullable = false)
    private int totalListagem;

    @Column(nullable = false)
    private int novos;

    @Column(nullable = false)
    private int atualizados;

    @Column(nullable = false)
    private int encerrados;

    @Column(nullable = false)
    private int descartados;

    @Column(name = "detalhes_baixados", nullable = false)
    private int detalhesBaixados;

    @Column(nullable = false)
    private int pendentes;

    @Column(columnDefinition = "TEXT")
    private String avisos;

    @Column(name = "mensagem_erro", columnDefinition = "TEXT")
    private String mensagemErro;

    public Long getId() {
        return id;
    }

    public Instant getIniciadaEm() {
        return iniciadaEm;
    }

    public void setIniciadaEm(Instant iniciadaEm) {
        this.iniciadaEm = iniciadaEm;
    }

    public Instant getFinalizadaEm() {
        return finalizadaEm;
    }

    public void setFinalizadaEm(Instant finalizadaEm) {
        this.finalizadaEm = finalizadaEm;
    }

    public StatusColeta getStatus() {
        return status;
    }

    public void setStatus(StatusColeta status) {
        this.status = status;
    }

    public OrigemColeta getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemColeta origem) {
        this.origem = origem;
    }

    public int getTotalListagem() {
        return totalListagem;
    }

    public void setTotalListagem(int totalListagem) {
        this.totalListagem = totalListagem;
    }

    public int getNovos() {
        return novos;
    }

    public void setNovos(int novos) {
        this.novos = novos;
    }

    public int getAtualizados() {
        return atualizados;
    }

    public void setAtualizados(int atualizados) {
        this.atualizados = atualizados;
    }

    public int getEncerrados() {
        return encerrados;
    }

    public void setEncerrados(int encerrados) {
        this.encerrados = encerrados;
    }

    public int getDescartados() {
        return descartados;
    }

    public void setDescartados(int descartados) {
        this.descartados = descartados;
    }

    public int getDetalhesBaixados() {
        return detalhesBaixados;
    }

    public void setDetalhesBaixados(int detalhesBaixados) {
        this.detalhesBaixados = detalhesBaixados;
    }

    public int getPendentes() {
        return pendentes;
    }

    public void setPendentes(int pendentes) {
        this.pendentes = pendentes;
    }

    public String getAvisos() {
        return avisos;
    }

    public void setAvisos(String avisos) {
        this.avisos = avisos;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    public void setMensagemErro(String mensagemErro) {
        this.mensagemErro = mensagemErro;
    }
}
