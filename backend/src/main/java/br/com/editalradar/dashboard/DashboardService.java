package br.com.editalradar.dashboard;

import br.com.editalradar.coleta.ColetaRepository;
import br.com.editalradar.concurso.ConcursoRepository;
import br.com.editalradar.concurso.StatusConcurso;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final int TOP_BANCAS = 10;
    private static final Duration JANELA_NOVOS = Duration.ofDays(7);
    private static final int DIAS_ENCERRANDO = 7;

    private static final List<FaixaSalarial> FAIXAS = List.of(
            new FaixaSalarial("Até R$ 3 mil", null, new BigDecimal("3000")),
            new FaixaSalarial("R$ 3–5 mil", new BigDecimal("3000"), new BigDecimal("5000")),
            new FaixaSalarial("R$ 5–8 mil", new BigDecimal("5000"), new BigDecimal("8000")),
            new FaixaSalarial("R$ 8–12 mil", new BigDecimal("8000"), new BigDecimal("12000")),
            new FaixaSalarial("R$ 12–20 mil", new BigDecimal("12000"), new BigDecimal("20000")),
            new FaixaSalarial("Acima de R$ 20 mil", new BigDecimal("20000"), null));

    private final ConcursoRepository concursos;
    private final ColetaRepository coletas;
    private final Clock clock;

    public DashboardService(ConcursoRepository concursos, ColetaRepository coletas, Clock clock) {
        this.concursos = concursos;
        this.coletas = coletas;
        this.clock = clock;
    }

    public ResumoDto resumo() {
        LocalDate hoje = LocalDate.now(clock);
        Instant agora = Instant.now(clock);
        return new ResumoDto(
                concursos.countByStatus(StatusConcurso.ABERTO),
                concursos.countByStatus(StatusConcurso.PREVISTO),
                concursos.countByStatusAndFimInscricaoBetween(StatusConcurso.ABERTO, hoje, hoje.plusDays(DIAS_ENCERRANDO)),
                concursos.countByStatusInAndPrimeiraVezVistoEmGreaterThanEqual(
                        List.of(StatusConcurso.ABERTO, StatusConcurso.PREVISTO), agora.minus(JANELA_NOVOS)),
                coletas.findFirstByOrderByIniciadaEmDesc().map(UltimaColetaDto::de).orElse(null));
    }

    public List<TotalPorUf> porUf() {
        Map<String, Long> totais = new LinkedHashMap<>();
        for (Object[] linha : concursos.contarPorUf(StatusConcurso.ABERTO)) {
            String uf = (String) linha[0];
            boolean nacional = Boolean.TRUE.equals(linha[1]);
            String rotulo = nacional ? "NACIONAL" : (uf == null ? "NÃO INFORMADA" : uf);
            totais.merge(rotulo, ((Number) linha[2]).longValue(), Long::sum);
        }
        return totais.entrySet().stream()
                .map(total -> new TotalPorUf(total.getKey(), total.getValue()))
                .sorted(Comparator.comparingLong(TotalPorUf::total).reversed().thenComparing(TotalPorUf::uf))
                .toList();
    }

    public List<TotalPorBanca> porBanca() {
        long naoInformada = 0;
        List<TotalPorBanca> informadas = new ArrayList<>();
        for (Object[] linha : concursos.contarPorBanca(StatusConcurso.ABERTO)) {
            String banca = (String) linha[0];
            long total = ((Number) linha[1]).longValue();
            if (banca == null || banca.isBlank()) {
                naoInformada += total;
            } else {
                informadas.add(new TotalPorBanca(banca, total));
            }
        }
        informadas.sort(Comparator.comparingLong(TotalPorBanca::total).reversed().thenComparing(TotalPorBanca::banca));

        List<TotalPorBanca> resultado = new ArrayList<>(informadas.subList(0, Math.min(TOP_BANCAS, informadas.size())));
        long outras = informadas.stream().skip(TOP_BANCAS).mapToLong(TotalPorBanca::total).sum();
        if (outras > 0) {
            resultado.add(new TotalPorBanca("Outras", outras));
        }
        if (naoInformada > 0) {
            resultado.add(new TotalPorBanca("Não informada", naoInformada));
        }
        return resultado;
    }

    public List<TotalPorFaixa> porFaixaSalarial() {
        long[] totais = new long[FAIXAS.size()];
        long naoInformado = 0;
        for (BigDecimal salario : concursos.salariosMaximos(StatusConcurso.ABERTO)) {
            if (salario == null) {
                naoInformado++;
                continue;
            }
            for (int i = 0; i < FAIXAS.size(); i++) {
                if (FAIXAS.get(i).contem(salario)) {
                    totais[i]++;
                    break;
                }
            }
        }
        List<TotalPorFaixa> resultado = new ArrayList<>();
        for (int i = 0; i < FAIXAS.size(); i++) {
            resultado.add(new TotalPorFaixa(FAIXAS.get(i).rotulo(), totais[i]));
        }
        resultado.add(new TotalPorFaixa("Não informado", naoInformado));
        return resultado;
    }
}
