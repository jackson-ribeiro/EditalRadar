package br.com.editalradar.dashboard;

import br.com.editalradar.coleta.ColetaRepository;
import br.com.editalradar.concurso.ConcursoRepository;
import br.com.editalradar.concurso.StatusConcurso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    private static final Instant AGORA = Instant.parse("2026-09-23T10:00:00Z");
    private static final Clock RELOGIO = Clock.fixed(AGORA, ZoneId.of("America/Sao_Paulo"));

    private final ConcursoRepository concursos = mock(ConcursoRepository.class);
    private final ColetaRepository coletas = mock(ColetaRepository.class);
    private DashboardService servico;

    @BeforeEach
    void preparar() {
        servico = new DashboardService(concursos, coletas, RELOGIO);
    }

    @Test
    void resumoUsaJanelasCorretas() {
        when(concursos.countByStatus(StatusConcurso.ABERTO)).thenReturn(12L);
        when(concursos.countByStatus(StatusConcurso.PREVISTO)).thenReturn(2L);
        when(concursos.countByStatusAndFimInscricaoBetween(StatusConcurso.ABERTO,
                LocalDate.of(2026, 9, 23), LocalDate.of(2026, 9, 30))).thenReturn(3L);
        when(concursos.countByStatusInAndPrimeiraVezVistoEmGreaterThanEqual(
                List.of(StatusConcurso.ABERTO, StatusConcurso.PREVISTO), Instant.parse("2026-09-16T10:00:00Z")))
                .thenReturn(4L);
        when(coletas.findFirstByOrderByIniciadaEmDesc()).thenReturn(Optional.empty());

        ResumoDto resumo = servico.resumo();

        assertThat(resumo).isEqualTo(new ResumoDto(12, 2, 3, 4, null));
    }

    @Test
    void porUfSeparaNacional() {
        when(concursos.contarPorUf(StatusConcurso.ABERTO)).thenReturn(linhas(
                new Object[]{"SP", false, 5L},
                new Object[]{null, true, 2L},
                new Object[]{"MG", false, 5L},
                new Object[]{"DF", false, 1L}));

        assertThat(servico.porUf()).containsExactly(
                new TotalPorUf("MG", 5), new TotalPorUf("SP", 5), new TotalPorUf("NACIONAL", 2),
                new TotalPorUf("DF", 1));
    }

    @Test
    void porBancaAgrupaTop10OutrasENaoInformada() {
        List<Object[]> linhas = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            linhas.add(new Object[]{"Banca " + (char) ('A' + i - 1), (long) (20 - i)});
        }
        linhas.add(new Object[]{null, 4L});
        when(concursos.contarPorBanca(StatusConcurso.ABERTO)).thenReturn(linhas);

        List<TotalPorBanca> resultado = servico.porBanca();

        assertThat(resultado).hasSize(12);
        assertThat(resultado.get(0)).isEqualTo(new TotalPorBanca("Banca A", 19));
        assertThat(resultado.get(9)).isEqualTo(new TotalPorBanca("Banca J", 10));
        assertThat(resultado.get(10)).isEqualTo(new TotalPorBanca("Outras", 9 + 8));
        assertThat(resultado.get(11)).isEqualTo(new TotalPorBanca("Não informada", 4));
    }

    @Test
    void porBancaOmiteOutrasENaoInformadaQuandoZero() {
        when(concursos.contarPorBanca(StatusConcurso.ABERTO)).thenReturn(linhas(new Object[]{"Quadrix", 3L}));

        assertThat(servico.porBanca()).containsExactly(new TotalPorBanca("Quadrix", 3));
    }

    @Test
    void porFaixaSalarialTrazTodasAsFaixasComLimitesCorretos() {
        when(concursos.salariosMaximos(StatusConcurso.ABERTO)).thenReturn(Arrays.asList(
                new BigDecimal("2999.99"), new BigDecimal("3000.00"), new BigDecimal("8000.00"),
                new BigDecimal("25000.00"), null));

        assertThat(servico.porFaixaSalarial()).containsExactly(
                new TotalPorFaixa("Até R$ 3 mil", 1),
                new TotalPorFaixa("R$ 3–5 mil", 1),
                new TotalPorFaixa("R$ 5–8 mil", 0),
                new TotalPorFaixa("R$ 8–12 mil", 1),
                new TotalPorFaixa("R$ 12–20 mil", 0),
                new TotalPorFaixa("Acima de R$ 20 mil", 1),
                new TotalPorFaixa("Não informado", 1));
    }

    private static List<Object[]> linhas(Object[]... linhas) {
        return Arrays.asList(linhas);
    }
}
