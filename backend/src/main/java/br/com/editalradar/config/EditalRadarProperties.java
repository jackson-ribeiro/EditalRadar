package br.com.editalradar.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "editalradar")
public record EditalRadarProperties(
        @Valid @NotNull Cors cors,
        @Valid @NotNull Coleta coleta,
        @Valid @NotNull Ti ti
) {

    public record Cors(List<String> origensPermitidas) {
        public Cors {
            origensPermitidas = origensPermitidas == null ? List.of() : List.copyOf(origensPermitidas);
        }
    }

    public record Coleta(
            boolean habilitada,
            @NotBlank String cron,
            @NotBlank String zona,
            @NotBlank String baseUrl,
            @NotBlank String userAgent,
            @NotNull Duration delayMin,
            @NotNull Duration delayMax,
            @NotNull Duration timeout,
            @Min(1) int maxTentativas,
            @Min(0) int detalhesPorExecucao,
            @Valid @NotNull Mcp mcp,
            @Valid @NotNull Previstos previstos
    ) {
    }

    public record Mcp(boolean habilitado, @NotBlank String url, List<String> termos) {
        public Mcp {
            termos = termos == null ? List.of() : List.copyOf(termos);
        }
    }

    public record Previstos(boolean habilitado, @Min(1) int maxIdadeDias) {
    }

    public record Ti(List<String> palavrasChave, List<String> exclusoes, List<String> termosEnsino) {
        public Ti {
            palavrasChave = palavrasChave == null ? List.of() : List.copyOf(palavrasChave);
            exclusoes = exclusoes == null ? List.of() : List.copyOf(exclusoes);
            termosEnsino = termosEnsino == null ? List.of() : List.copyOf(termosEnsino);
        }
    }
}
