package br.com.editalradar.concurso;

import br.com.editalradar.comum.Textos;
import br.com.editalradar.web.PaginaDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/concursos")
public class ConcursoController {

    private final ConcursoService servico;

    public ConcursoController(ConcursoService servico) {
        this.servico = servico;
    }

    @GetMapping
    public PaginaDto<ConcursoDto> listar(
            @RequestParam(required = false) String uf,
            @RequestParam(defaultValue = "ABERTO") StatusConcurso status,
            @RequestParam(required = false) String banca,
            @RequestParam(required = false) @PositiveOrZero BigDecimal salarioMin,
            @RequestParam(required = false) String escolaridade,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "fimInscricao,asc") String sort) {
        ConcursoFiltro filtro = new ConcursoFiltro(Textos.vazioComoNulo(uf), status, Textos.vazioComoNulo(banca),
                salarioMin, Textos.vazioComoNulo(escolaridade), Textos.vazioComoNulo(q));
        return servico.listar(filtro, page, size, sort);
    }
}
