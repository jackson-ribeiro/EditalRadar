package br.com.editalradar.coleta;

import br.com.editalradar.config.EditalRadarProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@EnableConfigurationProperties(EditalRadarProperties.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SincronizacaoExecutor executor;

    @MockitoBean
    private ColetaRepository coletas;

    @Test
    void syncRetorna202ComIdDaColeta() throws Exception {
        when(executor.disparar(OrigemColeta.MANUAL)).thenReturn(Optional.of(7L));

        mvc.perform(post("/api/admin/sync"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.coletaId").value(7));
    }

    @Test
    void syncEmAndamentoRetorna409() throws Exception {
        when(executor.disparar(OrigemColeta.MANUAL)).thenReturn(Optional.empty());

        mvc.perform(post("/api/admin/sync"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Coleta em andamento"));
    }

    @Test
    void ultimaColetaRetornaDados() throws Exception {
        Coleta coleta = new Coleta();
        coleta.setStatus(StatusColeta.SUCESSO);
        coleta.setOrigem(OrigemColeta.AGENDADA);
        coleta.setIniciadaEm(Instant.parse("2026-09-23T10:00:00Z"));
        coleta.setNovos(3);
        coleta.setAvisos("aviso 1\naviso 2");
        when(coletas.findFirstByOrderByIniciadaEmDesc()).thenReturn(Optional.of(coleta));

        mvc.perform(get("/api/admin/coletas/ultima"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCESSO"))
                .andExpect(jsonPath("$.novos").value(3))
                .andExpect(jsonPath("$.avisos.length()").value(2))
                .andExpect(jsonPath("$.iniciadaEm").value("2026-09-23T10:00:00Z"));
    }

    @Test
    void semColetaRetorna204() throws Exception {
        when(coletas.findFirstByOrderByIniciadaEmDesc()).thenReturn(Optional.empty());

        mvc.perform(get("/api/admin/coletas/ultima")).andExpect(status().isNoContent());
    }
}
