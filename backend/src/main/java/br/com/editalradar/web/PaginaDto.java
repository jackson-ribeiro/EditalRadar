package br.com.editalradar.web;

import org.springframework.data.domain.Page;

import java.util.List;

public record PaginaDto<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PaginaDto<T> de(Page<T> pagina) {
        return new PaginaDto<>(pagina.getContent(), pagina.getNumber(), pagina.getSize(),
                pagina.getTotalElements(), pagina.getTotalPages());
    }
}
