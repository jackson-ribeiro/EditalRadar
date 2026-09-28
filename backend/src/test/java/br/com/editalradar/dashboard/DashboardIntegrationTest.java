package br.com.editalradar.dashboard;

import br.com.editalradar.TestcontainersConfiguration;
import br.com.editalradar.concurso.Concurso;
import br.com.editalradar.concurso.ConcursoRepository;
import br.com.editalradar.concurso.OrigemTi;
import br.com.editalradar.concurso.StatusConcurso;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
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
class DashboardIntegrationTest {

    @Autowired
    private ConcursoRepository repositorio;

    @Test
    void consultasDeAgregacaoRodamNoPostgres() {
        Instant agora = Instant.parse("2026-09-23T10:00:00Z");
        repositorio.saveAll(List.of(
                concurso("a", "SP", false, "Quadrix", "8000.00", StatusConcurso.ABERTO, LocalDate.of(2026, 9, 25), agora),
                concurso("b", null, true, null, null, StatusConcurso.ABERTO, null, agora),
                concurso("c", "SP", false, "Quadrix", "3000.00", StatusConcurso.ENCERRADO, null, agora)));

        assertThat(repositorio.countByStatus(StatusConcurso.ABERTO)).isEqualTo(2);
        assertThat(repositorio.countByStatusAndFimInscricaoBetween(StatusConcurso.ABERTO,
                LocalDate.of(2026, 9, 23), LocalDate.of(2026, 9, 30))).isEqualTo(1);
        assertThat(repositorio.countByStatusInAndPrimeiraVezVistoEmGreaterThanEqual(
                List.of(StatusConcurso.ABERTO, StatusConcurso.PREVISTO), agora.minusSeconds(60))).isEqualTo(2);
        assertThat(repositorio.contarPorUf(StatusConcurso.ABERTO)).hasSize(2);
        assertThat(repositorio.contarPorBanca(StatusConcurso.ABERTO)).hasSize(2);
        assertThat(repositorio.salariosMaximos(StatusConcurso.ABERTO)).hasSize(2);
    }

    private static Concurso concurso(String slug, String uf, boolean nacional, String banca, String salario,
                                     StatusConcurso status, LocalDate fim, Instant agora) {
        Concurso concurso = new Concurso();
        concurso.setUrlOrigem("https://www.pciconcursos.com.br/noticias/" + slug);
        concurso.setOrgao("Órgão " + slug);
        concurso.setUf(uf);
        concurso.setNacional(nacional);
        concurso.setBanca(banca);
        concurso.setSalarioMax(salario == null ? null : new BigDecimal(salario));
        concurso.setStatus(status);
        concurso.setFimInscricao(fim);
        concurso.setOrigemTi(OrigemTi.LISTAGEM);
        concurso.setPrimeiraVezVistoEm(agora);
        concurso.setUltimaVezVistoEm(agora);
        concurso.setAtualizadoEm(agora);
        return concurso;
    }
}
