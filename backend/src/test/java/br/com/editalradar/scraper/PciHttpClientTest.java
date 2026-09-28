package br.com.editalradar.scraper;

import br.com.editalradar.config.EditalRadarProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PciHttpClientTest {

    private HttpServer servidor;
    private String base;
    private final Deque<Integer> statusRespostas = new ArrayDeque<>();
    private final List<String> userAgents = new CopyOnWriteArrayList<>();
    private final List<String> tiposConteudo = new CopyOnWriteArrayList<>();
    private final List<Duration> pausas = new ArrayList<>();

    @BeforeEach
    void iniciarServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", troca -> {
            userAgents.add(troca.getRequestHeaders().getFirst("User-Agent"));
            tiposConteudo.add(troca.getRequestHeaders().getFirst("Content-Type"));
            byte[] corpoRecebido = troca.getRequestBody().readAllBytes();
            Integer status = statusRespostas.poll();
            int codigo = status == null ? 200 : status;
            byte[] resposta = "GET".equals(troca.getRequestMethod())
                    ? "<html>olá</html>".getBytes(StandardCharsets.UTF_8)
                    : corpoRecebido;
            troca.sendResponseHeaders(codigo, resposta.length);
            try (OutputStream saida = troca.getResponseBody()) {
                saida.write(resposta);
            }
        });
        servidor.start();
        base = "http://127.0.0.1:" + servidor.getAddress().getPort();
    }

    @AfterEach
    void pararServidor() {
        servidor.stop(0);
    }

    @Test
    void getRetornaCorpoEmUtf8EEnviaUserAgent() {
        assertThat(cliente().get(base + "/concursos/")).isEqualTo("<html>olá</html>");
        assertThat(userAgents).containsExactly("EditalRadar/teste");
        assertThat(pausas).isEmpty();
    }

    @Test
    void tentaNovamenteEmErro5xxComBackoff() {
        statusRespostas.addAll(List.of(500, 503));

        assertThat(cliente().get(base + "/x")).isEqualTo("<html>olá</html>");
        assertThat(userAgents).hasSize(3);
        assertThat(pausas).contains(Duration.ofSeconds(4), Duration.ofSeconds(8));
    }

    @Test
    void naoTentaNovamenteEm404() {
        statusRespostas.add(404);

        assertThatThrownBy(() -> cliente().get(base + "/nao-existe"))
                .isInstanceOf(HttpColetaException.class)
                .hasMessageContaining("HTTP 404");
        assertThat(userAgents).hasSize(1);
    }

    @Test
    void desisteAposMaximoDeTentativas() {
        statusRespostas.addAll(List.of(503, 503, 503));

        assertThatThrownBy(() -> cliente().get(base + "/fora"))
                .isInstanceOf(HttpColetaException.class)
                .hasMessageContaining("HTTP 503");
        assertThat(userAgents).hasSize(3);
    }

    @Test
    void respeitaIntervaloEntreRequisicoes() {
        PciHttpClient cliente = cliente();
        cliente.get(base + "/a");
        cliente.get(base + "/b");

        assertThat(pausas).hasSize(1);
        assertThat(pausas.get(0)).isBetween(Duration.ofMillis(900), Duration.ofMillis(2000));
    }

    @Test
    void postJsonEnviaCorpoECabecalhos() {
        assertThat(cliente().postJson(base + "/mcp", "{\"a\":1}")).isEqualTo("{\"a\":1}");
        assertThat(tiposConteudo).containsExactly("application/json");
    }

    private PciHttpClient cliente() {
        EditalRadarProperties.Coleta config = new EditalRadarProperties.Coleta(
                true, "0 0 7 * * *", "America/Sao_Paulo", base, "EditalRadar/teste",
                Duration.ofMillis(1000), Duration.ofMillis(2000), Duration.ofSeconds(5), 3, 600,
                new EditalRadarProperties.Mcp(false, base + "/mcp", List.of()),
                new EditalRadarProperties.Previstos(false, 180));
        return new PciHttpClient(config, pausas::add, new Random(42));
    }
}
