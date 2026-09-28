package br.com.editalradar.concurso;

import br.com.editalradar.comum.Ufs;
import br.com.editalradar.ti.FiltroTi;
import br.com.editalradar.web.PaginaDto;
import br.com.editalradar.web.ParametroInvalidoException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class ConcursoService {

    private final ConcursoRepository concursos;
    private final FiltroTi filtroTi;
    private final Clock clock;

    public ConcursoService(ConcursoRepository concursos, FiltroTi filtroTi, Clock clock) {
        this.concursos = concursos;
        this.filtroTi = filtroTi;
        this.clock = clock;
    }

    public PaginaDto<ConcursoDto> listar(ConcursoFiltro filtro, int pagina, int tamanho, String ordenacao) {
        if (filtro.uf() != null && !filtro.uf().equalsIgnoreCase("NACIONAL") && !Ufs.valida(filtro.uf())) {
            throw new ParametroInvalidoException("UF inválida: '" + filtro.uf() + "'. Use uma sigla (ex.: SP) ou NACIONAL.");
        }
        OrdenacaoConcurso ordem = OrdenacaoConcurso.de(ordenacao);
        LocalDate hoje = LocalDate.now(clock);
        Instant agora = Instant.now(clock);
        return PaginaDto.de(concursos
                .findAll(ConcursoSpecifications.comFiltro(filtro).and(ConcursoSpecifications.ordenadoPor(ordem)),
                        PageRequest.of(pagina, tamanho))
                .map(concurso -> ConcursoDto.de(concurso, hoje, agora, cargo -> filtroTi.ehTi(cargo))));
    }
}
