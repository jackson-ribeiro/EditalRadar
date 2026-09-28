package br.com.editalradar.concurso;

import br.com.editalradar.config.EditalRadarProperties;
import br.com.editalradar.web.PaginaDto;
import br.com.editalradar.web.ParametroInvalidoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConcursoController.class)
@EnableConfigurationProperties(EditalRadarProperties.class)
class ConcursoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ConcursoService servico;

    @Test
    void repassaFiltrosPaginacaoEOrdenacao() throws Exception {
        when(servico.listar(any(), anyInt(), anyInt(), anyString()))
                .thenReturn(new PaginaDto<>(List.of(), 1, 10, 0, 0));

        mvc.perform(get("/api/concursos")
                        .param("uf", "sp").param("status", "ABERTO").param("banca", "Quadrix")
                        .param("salarioMin", "5000").param("escolaridade", "superior").param("q", "analista")
                        .param("page", "1").param("size", "10").param("sort", "salarioMax,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10));

        verify(servico).listar(new ConcursoFiltro("sp", StatusConcurso.ABERTO, "Quadrix", new BigDecimal("5000"),
                "superior", "analista"), 1, 10, "salarioMax,desc");
    }

    @Test
    void usaPadroesQuandoSemParametros() throws Exception {
        when(servico.listar(any(), anyInt(), anyInt(), anyString()))
                .thenReturn(new PaginaDto<>(List.of(), 0, 20, 0, 0));

        mvc.perform(get("/api/concursos")).andExpect(status().isOk());

        verify(servico).listar(new ConcursoFiltro(null, StatusConcurso.ABERTO, null, null, null, null),
                0, 20, "fimInscricao,asc");
    }

    @Test
    void rejeitaTamanhoDePaginaAcimaDe100() throws Exception {
        mvc.perform(get("/api/concursos").param("size", "101")).andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaStatusInvalido() throws Exception {
        mvc.perform(get("/api/concursos").param("status", "QUALQUER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parâmetro inválido"));
    }

    @Test
    void ufInvalidaViraBadRequest() throws Exception {
        when(servico.listar(any(), anyInt(), anyInt(), anyString()))
                .thenThrow(new ParametroInvalidoException("UF inválida: 'XX'."));

        mvc.perform(get("/api/concursos").param("uf", "XX"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("UF inválida: 'XX'."));
    }
}
