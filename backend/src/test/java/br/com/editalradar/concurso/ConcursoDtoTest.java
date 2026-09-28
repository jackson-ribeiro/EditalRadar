package br.com.editalradar.concurso;

import br.com.editalradar.scraper.DetalheParser;
import br.com.editalradar.suporte.Fixtures;
import br.com.editalradar.suporte.PropriedadesTeste;
import br.com.editalradar.ti.FiltroTi;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class ConcursoDtoTest {

    private static final Instant AGORA = Instant.parse("2026-09-23T10:00:00Z");
    private static final LocalDate HOJE = LocalDate.of(2026, 9, 23);
    private static final FiltroTi FILTRO = new FiltroTi(PropriedadesTeste.PALAVRAS, PropriedadesTeste.EXCLUSOES);
    private static final Predicate<String> EH_TI = cargo -> FILTRO.ehTi(cargo);

    @Test
    void calculaNovoDiasRestantesECargos() {
        Concurso concurso = new Concurso();
        concurso.setOrgao("Câmara");
        concurso.setStatus(StatusConcurso.ABERTO);
        concurso.setOrigemTi(OrigemTi.DETALHE);
        concurso.setCargos("Analista de TI\n\nContador");
        concurso.setFimInscricao(LocalDate.of(2026, 9, 30));
        concurso.setPrimeiraVezVistoEm(Instant.parse("2026-09-20T10:00:00Z"));

        ConcursoDto dto = ConcursoDto.de(concurso, HOJE, AGORA, EH_TI);

        assertThat(dto.novo()).isTrue();
        assertThat(dto.diasRestantes()).isEqualTo(7L);
        assertThat(dto.cargos()).containsExactly("Analista de TI", "Contador");
        assertThat(dto.cargosTi()).containsExactly("Analista de TI");
    }

    @Test
    void antigoSemPrazoESemCargos() {
        Concurso concurso = new Concurso();
        concurso.setOrgao("Câmara");
        concurso.setStatus(StatusConcurso.ABERTO);
        concurso.setOrigemTi(OrigemTi.LISTAGEM);
        concurso.setPrimeiraVezVistoEm(Instant.parse("2026-09-01T10:00:00Z"));

        ConcursoDto dto = ConcursoDto.de(concurso, HOJE, AGORA, EH_TI);

        assertThat(dto.novo()).isFalse();
        assertThat(dto.diasRestantes()).isNull();
        assertThat(dto.cargos()).isEmpty();
        assertThat(dto.cargosTi()).isEmpty();
        assertThat(dto.trechoTaxa()).isNull();
    }

    @Test
    void cargosTiDeContagemSaoOsDoisDeTi() {
        Concurso concurso = new Concurso();
        concurso.setOrgao("Prefeitura de Contagem");
        concurso.setStatus(StatusConcurso.ABERTO);
        concurso.setOrigemTi(OrigemTi.DETALHE);
        concurso.setPrimeiraVezVistoEm(AGORA);
        concurso.setCargos(String.join("\n", new DetalheParser().parse(Fixtures.ler("detalhe-prefeitura-contagem.html")).cargos()));
        concurso.setTrechoTaxa("A taxa de inscrição é de R$ 70,00 para nível médio/técnico e de R$ 100,00 para nível superior.");
        concurso.setTaxaInscricao(new BigDecimal("70.00"));

        ConcursoDto dto = ConcursoDto.de(concurso, HOJE, AGORA, EH_TI);

        assertThat(dto.cargos()).hasSize(36);
        assertThat(dto.cargosTi()).containsExactly(
                "Analista de TI (1 vaga)", "Auditor de Controle Interno - Tecnologia da Informação (1 vaga)");
        assertThat(dto.trechoTaxa()).startsWith("A taxa de inscrição é de R$ 70,00");
        assertThat(dto.taxaInscricao()).isEqualByComparingTo("70.00");
    }
}
