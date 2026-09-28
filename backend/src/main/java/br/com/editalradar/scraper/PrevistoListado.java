package br.com.editalradar.scraper;

import java.time.LocalDate;

public record PrevistoListado(String url, String titulo, LocalDate dataPublicacao) {
}
