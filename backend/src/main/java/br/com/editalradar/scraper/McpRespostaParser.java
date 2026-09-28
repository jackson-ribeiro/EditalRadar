package br.com.editalradar.scraper;

import br.com.editalradar.comum.Textos;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class McpRespostaParser {

    private final ObjectMapper mapper;

    public McpRespostaParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public List<ConcursoMcp> parse(String corpo) {
        try {
            JsonNode raiz = mapper.readTree(extrairJson(corpo));
            if (raiz.hasNonNull("error")) {
                throw new McpException("Erro JSON-RPC do MCP: " + raiz.get("error").path("message").asText());
            }
            JsonNode resultado = raiz.path("result");
            JsonNode texto = resultado.path("content").path(0).path("text");
            if (resultado.path("isError").asBoolean(false)) {
                throw new McpException("MCP retornou erro: " + texto.asText());
            }
            if (!texto.isTextual()) {
                throw new McpException("Resposta do MCP sem result.content[0].text");
            }
            JsonNode dados = mapper.readTree(texto.asText()).path("data");
            if (!dados.isArray()) {
                throw new McpException("Resposta do MCP sem o array data");
            }
            List<ConcursoMcp> itens = new ArrayList<>();
            for (JsonNode dado : dados) {
                String link = Textos.vazioComoNulo(dado.path("noticia").path("link").asText(null));
                if (link == null) {
                    continue;
                }
                List<String> cargos = new ArrayList<>();
                dado.path("cargos").forEach(cargo -> cargos.add(cargo.asText()));
                itens.add(new ConcursoMcp(link, cargos));
            }
            return List.copyOf(itens);
        } catch (JsonProcessingException e) {
            throw new McpException("JSON inválido na resposta do MCP: " + e.getOriginalMessage(), e);
        }
    }

    static String extrairJson(String corpo) {
        String texto = corpo == null ? "" : corpo.strip();
        if (texto.startsWith("{")) {
            return texto;
        }
        String ultimoDado = null;
        for (String linha : texto.split("\\R")) {
            if (linha.startsWith("data:")) {
                ultimoDado = linha.substring(5).strip();
            }
        }
        if (ultimoDado == null) {
            throw new McpException("Resposta do MCP em formato desconhecido");
        }
        return ultimoDado;
    }
}
