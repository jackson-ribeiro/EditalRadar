package br.com.editalradar.suporte;

import br.com.editalradar.config.EditalRadarProperties;

import java.time.Duration;
import java.util.List;

public final class PropriedadesTeste {

    public static final List<String> PALAVRAS = List.of(
            "analista de sistemas", "analista de ti", "tecnologia da informação", "desenvolvedor",
            "programador", "informática", "banco de dados", "segurança da informação",
            "ciência de dados", "suporte técnico");

    public static final List<String> EXCLUSOES = List.of(
            "noções de informática", "conhecimentos de informática", "informática básica",
            "professor de informática");

    public static final List<String> ENSINO = List.of(
            "professor", "docente", "peb", "educação básica", "bncc", "instrutor", "tutor");

    private PropriedadesTeste() {
    }

    public static EditalRadarProperties padrao() {
        return com(600, true, true);
    }

    public static EditalRadarProperties com(int detalhesPorExecucao, boolean mcpHabilitado, boolean previstosHabilitado) {
        return new EditalRadarProperties(
                new EditalRadarProperties.Cors(List.of("http://localhost:5173")),
                new EditalRadarProperties.Coleta(
                        true,
                        "0 0 7 * * *",
                        "America/Sao_Paulo",
                        "https://www.pciconcursos.com.br",
                        "EditalRadar/teste",
                        Duration.ofMillis(1000),
                        Duration.ofMillis(2000),
                        Duration.ofSeconds(5),
                        3,
                        detalhesPorExecucao,
                        new EditalRadarProperties.Mcp(mcpHabilitado, "https://mcp.pciconcursos.com.br/mcp",
                                List.of("analista de sistemas")),
                        new EditalRadarProperties.Previstos(previstosHabilitado, 180)),
                new EditalRadarProperties.Ti(PALAVRAS, EXCLUSOES, ENSINO));
    }
}
