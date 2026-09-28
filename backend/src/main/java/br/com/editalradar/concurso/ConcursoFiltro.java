package br.com.editalradar.concurso;

import java.math.BigDecimal;

public record ConcursoFiltro(
        String uf,
        StatusConcurso status,
        String banca,
        BigDecimal salarioMin,
        String escolaridade,
        String q
) {
}
