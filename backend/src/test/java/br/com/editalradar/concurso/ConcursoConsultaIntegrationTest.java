package br.com.editalradar.concurso;

import br.com.editalradar.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class ConcursoConsultaIntegrationTest {

    @Autowired
    private ConcursoRepository repositorio;

    @BeforeEach
    void popular() {
        repositorio.saveAll(List.of(
                concurso("sp-1", "SP", false, "Prefeitura de São Paulo", "Analista de Tecnologia da Informação",
                        "Quadrix", "8000.00", LocalDate.of(2026, 10, 10)),
                concurso("sp-2", "SP", false, "Câmara de Campinas", "Programador", null, null, null),
                concurso("mg-1", "MG", false, "Câmara de Unaí", "Analista de Sistemas", "Consulplan",
                        "9193.89", LocalDate.of(2026, 12, 1)),
                concurso("nac-1", null, true, "CRBio-01", "Vários Cargos", "Quadrix", "7181.50",
                        LocalDate.of(2026, 10, 21)),
                concurso("sp-3", "SP", false, "Órgão 100% digital", "Suporte_TI", null, "3000.00",
                        LocalDate.of(2026, 10, 1))));
    }

    @Test
    void filtroPorUfNaoTrazNacionais() {
        assertThat(buscar(new ConcursoFiltro("SP", StatusConcurso.ABERTO, null, null, null, null)))
                .extracting(Concurso::getOrgao)
                .containsExactlyInAnyOrder("Prefeitura de São Paulo", "Câmara de Campinas", "Órgão 100% digital");
    }

    @Test
    void filtroNacionalTrazSoNacionais() {
        assertThat(buscar(new ConcursoFiltro("NACIONAL", StatusConcurso.ABERTO, null, null, null, null)))
                .extracting(Concurso::getOrgao).containsExactly("CRBio-01");
    }

    @Test
    void buscaTextualIgnoraAcentosEMaiusculas() {
        assertThat(buscar(new ConcursoFiltro(null, StatusConcurso.ABERTO, null, null, null, "INFORMACAO")))
                .extracting(Concurso::getOrgao).containsExactly("Prefeitura de São Paulo");
        assertThat(buscar(new ConcursoFiltro(null, StatusConcurso.ABERTO, null, null, null, "camara")))
                .extracting(Concurso::getOrgao).containsExactlyInAnyOrder("Câmara de Campinas", "Câmara de Unaí");
    }

    @Test
    void curingasSaoLiterais() {
        assertThat(buscar(new ConcursoFiltro(null, StatusConcurso.ABERTO, null, null, null, "100%")))
                .extracting(Concurso::getOrgao).containsExactly("Órgão 100% digital");
        assertThat(buscar(new ConcursoFiltro(null, StatusConcurso.ABERTO, null, null, null, "de_tecnologia")))
                .isEmpty();
    }

    @Test
    void filtraBancaESalario() {
        assertThat(buscar(new ConcursoFiltro(null, StatusConcurso.ABERTO, "quadrix", new BigDecimal("7500"), null, null)))
                .extracting(Concurso::getOrgao).containsExactly("Prefeitura de São Paulo");
    }

    @Test
    void ordenaPorPrazoComNulosPorUltimo() {
        assertThat(buscarOrdenado(new OrdenacaoConcurso("fimInscricao", true)))
                .extracting(Concurso::getOrgao)
                .containsExactly("Órgão 100% digital", "Prefeitura de São Paulo", "CRBio-01", "Câmara de Unaí",
                        "Câmara de Campinas");
    }

    @Test
    void ordenaPorSalarioDecrescenteComNulosPorUltimo() {
        assertThat(buscarOrdenado(new OrdenacaoConcurso("salarioMax", false)))
                .extracting(Concurso::getOrgao)
                .containsExactly("Câmara de Unaí", "Prefeitura de São Paulo", "CRBio-01", "Órgão 100% digital",
                        "Câmara de Campinas");
    }

    @Test
    void paginaCorretamenteComOrdenacao() {
        var pagina = repositorio.findAll(
                ConcursoSpecifications.comFiltro(new ConcursoFiltro(null, StatusConcurso.ABERTO, null, null, null, null))
                        .and(ConcursoSpecifications.ordenadoPor(new OrdenacaoConcurso("fimInscricao", true))),
                PageRequest.of(1, 2));

        assertThat(pagina.getTotalElements()).isEqualTo(5);
        assertThat(pagina.getContent()).extracting(Concurso::getOrgao).containsExactly("CRBio-01", "Câmara de Unaí");
    }

    private List<Concurso> buscar(ConcursoFiltro filtro) {
        return repositorio.findAll(ConcursoSpecifications.comFiltro(filtro));
    }

    private List<Concurso> buscarOrdenado(OrdenacaoConcurso ordenacao) {
        return repositorio.findAll(ConcursoSpecifications.comFiltro(
                        new ConcursoFiltro(null, StatusConcurso.ABERTO, null, null, null, null))
                .and(ConcursoSpecifications.ordenadoPor(ordenacao)));
    }

    private static Concurso concurso(String slug, String uf, boolean nacional, String orgao, String cargo,
                                     String banca, String salarioMax, LocalDate fim) {
        Concurso concurso = new Concurso();
        concurso.setUrlOrigem("https://www.pciconcursos.com.br/noticias/" + slug);
        concurso.setUf(uf);
        concurso.setNacional(nacional);
        concurso.setOrgao(orgao);
        concurso.setCargo(cargo);
        concurso.setBanca(banca);
        concurso.setSalarioMax(salarioMax == null ? null : new BigDecimal(salarioMax));
        concurso.setFimInscricao(fim);
        concurso.setStatus(StatusConcurso.ABERTO);
        concurso.setOrigemTi(OrigemTi.LISTAGEM);
        Instant agora = Instant.parse("2026-09-23T10:00:00Z");
        concurso.setPrimeiraVezVistoEm(agora);
        concurso.setUltimaVezVistoEm(agora);
        concurso.setAtualizadoEm(agora);
        return concurso;
    }
}
