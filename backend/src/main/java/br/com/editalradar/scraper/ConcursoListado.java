package br.com.editalradar.scraper;

import br.com.editalradar.comum.Textos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConcursoListado(
        String urlOrigem,
        String orgao,
        String titulo,
        String uf,
        boolean nacional,
        String cargo,
        String escolaridade,
        Integer vagas,
        boolean cadastroReserva,
        BigDecimal salarioMin,
        BigDecimal salarioMax,
        LocalDate inicioInscricao,
        LocalDate fimInscricao,
        DatasTexto.SituacaoPrazo situacaoPrazo
) {

    public boolean variosCargos() {
        return cargo != null && Textos.normalizar(cargo).startsWith("varios cargos");
    }
}
