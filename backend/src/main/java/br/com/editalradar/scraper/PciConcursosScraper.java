package br.com.editalradar.scraper;

import br.com.editalradar.config.EditalRadarProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PciConcursosScraper {

    private final PciHttpClient http;
    private final ListagemParser listagemParser;
    private final DetalheParser detalheParser;
    private final PrevistosParser previstosParser;
    private final McpRespostaParser mcpParser;
    private final ObjectMapper mapper;
    private final String baseUrl;
    private final String mcpUrl;

    public PciConcursosScraper(PciHttpClient http, ListagemParser listagemParser, DetalheParser detalheParser,
                               PrevistosParser previstosParser, McpRespostaParser mcpParser, ObjectMapper mapper,
                               EditalRadarProperties propriedades) {
        this.http = http;
        this.listagemParser = listagemParser;
        this.detalheParser = detalheParser;
        this.previstosParser = previstosParser;
        this.mcpParser = mcpParser;
        this.mapper = mapper;
        this.baseUrl = semBarraFinal(propriedades.coleta().baseUrl());
        this.mcpUrl = propriedades.coleta().mcp().url();
    }

    public ResultadoListagem listarAbertos() {
        return listagemParser.parse(http.get(baseUrl + "/concursos/"));
    }

    public DetalheConcurso detalhar(String url) {
        if (url == null || !url.startsWith(baseUrl + "/")) {
            throw new IllegalArgumentException("URL fora do PCI Concursos: " + url);
        }
        return detalheParser.parse(http.get(url));
    }

    public List<PrevistoListado> listarPrevistos() {
        return previstosParser.parse(http.get(baseUrl + "/previstos/"));
    }

    public List<ConcursoMcp> buscarNoMcp(String termo) {
        return mcpParser.parse(http.postJson(mcpUrl, chamadaBuscarPorCargo(termo)));
    }

    private String chamadaBuscarPorCargo(String termo) {
        ObjectNode chamada = mapper.createObjectNode();
        chamada.put("jsonrpc", "2.0");
        chamada.put("id", 1);
        chamada.put("method", "tools/call");
        ObjectNode parametros = chamada.putObject("params");
        parametros.put("name", "buscar_por_cargo");
        parametros.putObject("arguments").put("cargo", termo);
        try {
            return mapper.writeValueAsString(chamada);
        } catch (JsonProcessingException e) {
            throw new McpException("Falha ao montar chamada ao MCP", e);
        }
    }

    private static String semBarraFinal(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
