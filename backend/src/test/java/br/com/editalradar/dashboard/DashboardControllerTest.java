package br.com.editalradar.dashboard;

import br.com.editalradar.config.EditalRadarProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@EnableConfigurationProperties(EditalRadarProperties.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private DashboardService servico;

    @Test
    void resumo() throws Exception {
        when(servico.resumo()).thenReturn(new ResumoDto(12, 2, 3, 4, null));

        mvc.perform(get("/api/dashboard/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.abertos").value(12))
                .andExpect(jsonPath("$.previstos").value(2))
                .andExpect(jsonPath("$.encerrandoEm7Dias").value(3))
                .andExpect(jsonPath("$.novosNaSemana").value(4));
    }

    @Test
    void agregacoes() throws Exception {
        when(servico.porUf()).thenReturn(List.of(new TotalPorUf("NACIONAL", 2)));
        when(servico.porBanca()).thenReturn(List.of(new TotalPorBanca("Quadrix", 3)));
        when(servico.porFaixaSalarial()).thenReturn(List.of(new TotalPorFaixa("Até R$ 3 mil", 1)));

        mvc.perform(get("/api/dashboard/por-uf")).andExpect(jsonPath("$[0].uf").value("NACIONAL"))
                .andExpect(jsonPath("$[0].total").value(2));
        mvc.perform(get("/api/dashboard/por-banca")).andExpect(jsonPath("$[0].banca").value("Quadrix"));
        mvc.perform(get("/api/dashboard/por-faixa-salarial")).andExpect(jsonPath("$[0].faixa").value("Até R$ 3 mil"));
    }
}
