package br.com.editalradar.scraper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DetalheConcurso(
        List<String> cargos,
        LocalDate inicioInscricao,
        LocalDate dataProva,
        String banca,
        BigDecimal salarioMin,
        BigDecimal salarioMax,
        String trechoRemuneracao,
        String trechoTaxa,
        BigDecimal taxaInscricao
) {

    public DetalheConcurso {
        cargos = cargos == null ? List.of() : List.copyOf(cargos);
    }
}
