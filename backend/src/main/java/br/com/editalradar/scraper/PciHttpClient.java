package br.com.editalradar.scraper;

import br.com.editalradar.config.EditalRadarProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Random;
import java.util.random.RandomGenerator;

@Component
public class PciHttpClient {

    @FunctionalInterface
    public interface Pausa {
        void pausar(Duration duracao) throws InterruptedException;
    }

    private static final Logger log = LoggerFactory.getLogger(PciHttpClient.class);

    private final EditalRadarProperties.Coleta config;
    private final Pausa pausa;
    private final RandomGenerator aleatorio;
    private final HttpClient http;
    private long ultimaRequisicaoNanos = -1;

    @Autowired
    public PciHttpClient(EditalRadarProperties propriedades) {
        this(propriedades.coleta(), Thread::sleep, new Random());
    }

    PciHttpClient(EditalRadarProperties.Coleta config, Pausa pausa, RandomGenerator aleatorio) {
        this.config = config;
        this.pausa = pausa;
        this.aleatorio = aleatorio;
        this.http = HttpClient.newBuilder()
                .connectTimeout(config.timeout())
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public synchronized String get(String url) {
        return executar(HttpRequest.newBuilder(URI.create(url))
                .GET()
                .header("Accept", "text/html,application/xhtml+xml"));
    }

    public synchronized String postJson(String url, String json) {
        return executar(HttpRequest.newBuilder(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream"));
    }

    private String executar(HttpRequest.Builder construtor) {
        HttpRequest requisicao = construtor
                .header("User-Agent", config.userAgent())
                .timeout(config.timeout())
                .build();
        HttpColetaException ultimaFalha = null;
        for (int tentativa = 1; tentativa <= config.maxTentativas(); tentativa++) {
            if (tentativa > 1) {
                pausar(config.delayMax().multipliedBy(1L << (tentativa - 1)));
            }
            respeitarIntervalo();
            try {
                HttpResponse<String> resposta = http.send(requisicao,
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                ultimaRequisicaoNanos = System.nanoTime();
                int status = resposta.statusCode();
                if (status >= 200 && status < 300) {
                    return resposta.body();
                }
                ultimaFalha = new HttpColetaException("HTTP " + status + " em " + requisicao.uri());
                if (status != 429 && status < 500) {
                    throw ultimaFalha;
                }
                log.warn("Tentativa {}/{} falhou com HTTP {} em {}", tentativa, config.maxTentativas(), status, requisicao.uri());
            } catch (IOException e) {
                ultimaRequisicaoNanos = System.nanoTime();
                ultimaFalha = new HttpColetaException("Falha de rede em " + requisicao.uri() + ": " + e.getMessage(), e);
                log.warn("Tentativa {}/{} falhou em {}: {}", tentativa, config.maxTentativas(), requisicao.uri(), e.toString());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new HttpColetaException("Requisição interrompida: " + requisicao.uri(), e);
            }
        }
        throw ultimaFalha;
    }

    private void respeitarIntervalo() {
        if (ultimaRequisicaoNanos < 0) {
            return;
        }
        long minimo = config.delayMin().toMillis();
        long maximo = config.delayMax().toMillis();
        long alvo = maximo > minimo ? aleatorio.nextLong(minimo, maximo + 1) : minimo;
        long decorrido = (System.nanoTime() - ultimaRequisicaoNanos) / 1_000_000;
        long restante = alvo - decorrido;
        if (restante > 0) {
            pausar(Duration.ofMillis(restante));
        }
    }

    private void pausar(Duration duracao) {
        try {
            pausa.pausar(duracao);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HttpColetaException("Pausa entre requisições interrompida", e);
        }
    }
}
