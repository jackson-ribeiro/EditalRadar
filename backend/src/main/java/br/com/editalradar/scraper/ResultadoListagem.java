package br.com.editalradar.scraper;

import java.util.List;

public record ResultadoListagem(List<ConcursoListado> itens, List<String> avisos) {

    public ResultadoListagem {
        itens = List.copyOf(itens);
        avisos = List.copyOf(avisos);
    }
}
