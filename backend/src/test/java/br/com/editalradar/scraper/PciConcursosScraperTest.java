package br.com.editalradar.scraper;

import br.com.editalradar.suporte.Fixtures;
import br.com.editalradar.suporte.PropriedadesTeste;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PciConcursosScraperTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private PciHttpClient http;
    private PciConcursosScraper scraper;

    @BeforeEach
    void preparar() {
        http = mock(PciHttpClient.class);
        scraper = new PciConcursosScraper(http, new ListagemParser(), new DetalheParser(), new PrevistosParser(),
                new McpRespostaParser(mapper), mapper, PropriedadesTeste.padrao());
    }

    @Test
    void listarAbertosBuscaAPaginaDeConcursos() {
        when(http.get("https://www.pciconcursos.com.br/concursos/")).thenReturn(Fixtures.ler("concursos-nacional.html"));

        assertThat(scraper.listarAbertos().itens()).hasSize(8);
    }

    @Test
    void detalharSoAceitaUrlsDoPci() {
        String url = "https://www.pciconcursos.com.br/noticias/crbio-01";
        when(http.get(url)).thenReturn(Fixtures.ler("detalhe-crbio01.html"));

        assertThat(scraper.detalhar(url).banca()).isEqualTo("Quadrix");
        assertThatThrownBy(() -> scraper.detalhar("https://outro.site/x"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void listarPrevistosBuscaAPrimeiraPagina() {
        when(http.get("https://www.pciconcursos.com.br/previstos/")).thenReturn(Fixtures.ler("previstos.html"));

        assertThat(scraper.listarPrevistos()).hasSize(150);
    }

    @Test
    void buscarNoMcpMontaChamadaJsonRpc() throws Exception {
        when(http.postJson(eq("https://mcp.pciconcursos.com.br/mcp"), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Fixtures.ler("mcp-buscar-por-cargo-analista-de-sistemas.json"));

        assertThat(scraper.buscarNoMcp("analista de sistemas")).hasSize(16);

        ArgumentCaptor<String> corpo = ArgumentCaptor.forClass(String.class);
        verify(http).postJson(eq("https://mcp.pciconcursos.com.br/mcp"), corpo.capture());
        JsonNode chamada = mapper.readTree(corpo.getValue());
        assertThat(chamada.path("jsonrpc").asText()).isEqualTo("2.0");
        assertThat(chamada.path("method").asText()).isEqualTo("tools/call");
        assertThat(chamada.path("params").path("name").asText()).isEqualTo("buscar_por_cargo");
        assertThat(chamada.path("params").path("arguments").path("cargo").asText()).isEqualTo("analista de sistemas");
    }
}
