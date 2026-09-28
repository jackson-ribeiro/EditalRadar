package br.com.editalradar.concurso;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public record ConcursoDto(
        Long id,
        String urlOrigem,
        String titulo,
        String orgao,
        String cargo,
        List<String> cargos,
        List<String> cargosTi,
        String uf,
        boolean nacional,
        String escolaridade,
        Integer vagas,
        boolean cadastroReserva,
        BigDecimal salarioMin,
        BigDecimal salarioMax,
        String trechoRemuneracao,
        String trechoTaxa,
        BigDecimal taxaInscricao,
        String banca,
        StatusConcurso status,
        LocalDate inicioInscricao,
        LocalDate fimInscricao,
        LocalDate dataProva,
        OrigemTi origemTi,
        Instant primeiraVezVistoEm,
        Instant atualizadoEm,
        boolean novo,
        Long diasRestantes
) {

    public static final Duration JANELA_NOVO = Duration.ofDays(7);

    public static ConcursoDto de(Concurso concurso, LocalDate hoje, Instant agora, Predicate<String> ehCargoTi) {
        List<String> cargos = concurso.getCargos() == null
                ? List.of()
                : Arrays.stream(concurso.getCargos().split("\n")).map(String::strip).filter(c -> !c.isEmpty()).toList();
        List<String> cargosTi = cargos.stream().filter(ehCargoTi).toList();
        boolean novo = concurso.getPrimeiraVezVistoEm() != null
                && !concurso.getPrimeiraVezVistoEm().isBefore(agora.minus(JANELA_NOVO));
        Long diasRestantes = concurso.getFimInscricao() == null
                ? null
                : ChronoUnit.DAYS.between(hoje, concurso.getFimInscricao());
        return new ConcursoDto(concurso.getId(), concurso.getUrlOrigem(), concurso.getTitulo(), concurso.getOrgao(),
                concurso.getCargo(), cargos, cargosTi, concurso.getUf(), concurso.isNacional(),
                concurso.getEscolaridade(), concurso.getVagas(), concurso.isCadastroReserva(), concurso.getSalarioMin(),
                concurso.getSalarioMax(), concurso.getTrechoRemuneracao(), concurso.getTrechoTaxa(),
                concurso.getTaxaInscricao(), concurso.getBanca(), concurso.getStatus(), concurso.getInicioInscricao(),
                concurso.getFimInscricao(), concurso.getDataProva(), concurso.getOrigemTi(),
                concurso.getPrimeiraVezVistoEm(), concurso.getAtualizadoEm(), novo, diasRestantes);
    }
}
