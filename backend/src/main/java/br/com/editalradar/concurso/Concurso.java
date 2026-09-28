package br.com.editalradar.concurso;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "concurso")
public class Concurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url_origem", nullable = false, unique = true, length = 512)
    private String urlOrigem;

    @Column(length = 500)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String orgao;

    @Column(length = 500)
    private String cargo;

    @Column(columnDefinition = "TEXT")
    private String cargos;

    @Column(length = 2)
    private String uf;

    @Column(nullable = false)
    private boolean nacional;

    @Column(length = 120)
    private String escolaridade;

    private Integer vagas;

    @Column(name = "cadastro_reserva", nullable = false)
    private boolean cadastroReserva;

    @Column(name = "salario_min", precision = 12, scale = 2)
    private BigDecimal salarioMin;

    @Column(name = "salario_max", precision = 12, scale = 2)
    private BigDecimal salarioMax;

    @Column(length = 120)
    private String banca;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConcurso status;

    @Column(name = "inicio_inscricao")
    private LocalDate inicioInscricao;

    @Column(name = "fim_inscricao")
    private LocalDate fimInscricao;

    @Column(name = "data_prova")
    private LocalDate dataProva;

    @Enumerated(EnumType.STRING)
    @Column(name = "origem_ti", nullable = false, length = 20)
    private OrigemTi origemTi;

    @Column(name = "primeira_vez_visto_em", nullable = false)
    private Instant primeiraVezVistoEm;

    @Column(name = "ultima_vez_visto_em", nullable = false)
    private Instant ultimaVezVistoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @Column(name = "detalhe_coletado_em")
    private Instant detalheColetadoEm;

    @Column(name = "trecho_remuneracao", columnDefinition = "TEXT")
    private String trechoRemuneracao;

    @Column(name = "trecho_taxa", columnDefinition = "TEXT")
    private String trechoTaxa;

    @Column(name = "taxa_inscricao", precision = 10, scale = 2)
    private BigDecimal taxaInscricao;

    public Long getId() {
        return id;
    }

    public String getUrlOrigem() {
        return urlOrigem;
    }

    public void setUrlOrigem(String urlOrigem) {
        this.urlOrigem = urlOrigem;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getOrgao() {
        return orgao;
    }

    public void setOrgao(String orgao) {
        this.orgao = orgao;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getCargos() {
        return cargos;
    }

    public void setCargos(String cargos) {
        this.cargos = cargos;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public boolean isNacional() {
        return nacional;
    }

    public void setNacional(boolean nacional) {
        this.nacional = nacional;
    }

    public String getEscolaridade() {
        return escolaridade;
    }

    public void setEscolaridade(String escolaridade) {
        this.escolaridade = escolaridade;
    }

    public Integer getVagas() {
        return vagas;
    }

    public void setVagas(Integer vagas) {
        this.vagas = vagas;
    }

    public boolean isCadastroReserva() {
        return cadastroReserva;
    }

    public void setCadastroReserva(boolean cadastroReserva) {
        this.cadastroReserva = cadastroReserva;
    }

    public BigDecimal getSalarioMin() {
        return salarioMin;
    }

    public void setSalarioMin(BigDecimal salarioMin) {
        this.salarioMin = salarioMin;
    }

    public BigDecimal getSalarioMax() {
        return salarioMax;
    }

    public void setSalarioMax(BigDecimal salarioMax) {
        this.salarioMax = salarioMax;
    }

    public String getBanca() {
        return banca;
    }

    public void setBanca(String banca) {
        this.banca = banca;
    }

    public StatusConcurso getStatus() {
        return status;
    }

    public void setStatus(StatusConcurso status) {
        this.status = status;
    }

    public LocalDate getInicioInscricao() {
        return inicioInscricao;
    }

    public void setInicioInscricao(LocalDate inicioInscricao) {
        this.inicioInscricao = inicioInscricao;
    }

    public LocalDate getFimInscricao() {
        return fimInscricao;
    }

    public void setFimInscricao(LocalDate fimInscricao) {
        this.fimInscricao = fimInscricao;
    }

    public LocalDate getDataProva() {
        return dataProva;
    }

    public void setDataProva(LocalDate dataProva) {
        this.dataProva = dataProva;
    }

    public OrigemTi getOrigemTi() {
        return origemTi;
    }

    public void setOrigemTi(OrigemTi origemTi) {
        this.origemTi = origemTi;
    }

    public Instant getPrimeiraVezVistoEm() {
        return primeiraVezVistoEm;
    }

    public void setPrimeiraVezVistoEm(Instant primeiraVezVistoEm) {
        this.primeiraVezVistoEm = primeiraVezVistoEm;
    }

    public Instant getUltimaVezVistoEm() {
        return ultimaVezVistoEm;
    }

    public void setUltimaVezVistoEm(Instant ultimaVezVistoEm) {
        this.ultimaVezVistoEm = ultimaVezVistoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(Instant atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public Instant getDetalheColetadoEm() {
        return detalheColetadoEm;
    }

    public void setDetalheColetadoEm(Instant detalheColetadoEm) {
        this.detalheColetadoEm = detalheColetadoEm;
    }

    public String getTrechoRemuneracao() {
        return trechoRemuneracao;
    }

    public void setTrechoRemuneracao(String trechoRemuneracao) {
        this.trechoRemuneracao = trechoRemuneracao;
    }

    public String getTrechoTaxa() {
        return trechoTaxa;
    }

    public void setTrechoTaxa(String trechoTaxa) {
        this.trechoTaxa = trechoTaxa;
    }

    public BigDecimal getTaxaInscricao() {
        return taxaInscricao;
    }

    public void setTaxaInscricao(BigDecimal taxaInscricao) {
        this.taxaInscricao = taxaInscricao;
    }
}
