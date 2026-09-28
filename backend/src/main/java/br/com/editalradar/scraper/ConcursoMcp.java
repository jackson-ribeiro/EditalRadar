package br.com.editalradar.scraper;

import java.util.List;

public record ConcursoMcp(String link, List<String> cargos) {

    public ConcursoMcp {
        cargos = cargos == null ? List.of() : List.copyOf(cargos);
    }
}
