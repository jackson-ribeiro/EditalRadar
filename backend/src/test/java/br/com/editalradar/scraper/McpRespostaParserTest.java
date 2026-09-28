package br.com.editalradar.scraper;

import br.com.editalradar.suporte.Fixtures;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class McpRespostaParserTest {

    private final McpRespostaParser parser = new McpRespostaParser(new ObjectMapper());

    @Test
    void leRespostaJsonDoFixture() {
        List<ConcursoMcp> itens = parser.parse(Fixtures.ler("mcp-buscar-por-cargo-analista-de-sistemas.json"));

        assertThat(itens).hasSize(16);
        assertThat(itens.get(0).link()).isEqualTo(
                "https://www.pciconcursos.com.br/noticias/camara-de-unai-mg-abre-concurso-publico-com-remuneracao-de-9193");
        assertThat(itens.get(0).cargos()).containsExactly(
                "ANALISTA DE ATIVIDADES DA SECRETARIA - ANALISTA DE SISTEMAS",
                "ANALISTA DE ATIVIDADES DA SECRETARIA - CONSULTOR JURÍDICO",
                "ANALISTA DE ATIVIDADES DA SECRETARIA - CONTADOR");
    }

    @Test
    void aceitaRespostaEmServerSentEvents() {
        String texto = "{\\\"meta\\\":{},\\\"data\\\":[{\\\"cargos\\\":[\\\"PROGRAMADOR\\\"],\\\"noticia\\\":{\\\"link\\\":\\\"https://www.pciconcursos.com.br/noticias/x\\\"}}]}";
        String corpo = "event: message\ndata: {\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{\"content\":[{\"type\":\"text\",\"text\":\"" + texto + "\"}]}}\n\n";

        assertThat(parser.parse(corpo)).containsExactly(
                new ConcursoMcp("https://www.pciconcursos.com.br/noticias/x", List.of("PROGRAMADOR")));
    }

    @Test
    void erroDoServidorViraMcpException() {
        assertThatThrownBy(() -> parser.parse("{\"jsonrpc\":\"2.0\",\"id\":1,\"error\":{\"code\":-32601,\"message\":\"Method not found\"}}"))
                .isInstanceOf(McpException.class)
                .hasMessageContaining("Method not found");
        assertThatThrownBy(() -> parser.parse("{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{\"isError\":true,\"content\":[{\"type\":\"text\",\"text\":\"Limite excedido\"}]}}"))
                .isInstanceOf(McpException.class)
                .hasMessageContaining("Limite excedido");
        assertThatThrownBy(() -> parser.parse("<html>502 Bad Gateway</html>"))
                .isInstanceOf(McpException.class);
    }
}
