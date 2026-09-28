package br.com.editalradar.ti;

import br.com.editalradar.suporte.PropriedadesTeste;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FiltroTiTest {

    private final FiltroTi filtro = new FiltroTi(PropriedadesTeste.PALAVRAS, PropriedadesTeste.EXCLUSOES);

    @ParameterizedTest
    @ValueSource(strings = {
            "Analista de TI (1 vaga)",
            "Técnico em Informática",
            "Auditor de Controle Interno - Tecnologia da Informação (1 vaga)",
            "Analista de Tecnologia da Informação (São Paulo - cadastro de reserva)",
            "ANALISTA DE ATIVIDADES DA SECRETARIA - ANALISTA DE SISTEMAS",
            "TECNOLOGIA DA INFORMACAO",
            "Programador"
    })
    void reconheceCargosDeTi(String texto) {
        assertThat(filtro.ehTi(texto)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Médico",
            "Guarda Civil Municipal",
            "Vários Cargos",
            "INCA solicita autorização para novo concurso com 1.496 vagas na área de Ciência e Tecnologia",
            "Prova objetiva com noções de informática e português",
            "Professor de Informática",
            "Analista de Tiro Esportivo"
    })
    void ignoraCargosForaDeTi(String texto) {
        assertThat(filtro.ehTi(texto)).isFalse();
    }

    @Test
    void aceitaVariosTextosENulos() {
        assertThat(filtro.ehTi(null, "Médico", "Analista de TI")).isTrue();
        assertThat(filtro.ehTi((String) null)).isFalse();
        assertThat(filtro.ehTi(List.of("Contador", "Programador"))).isTrue();
        assertThat(filtro.ehTi(List.of())).isFalse();
    }
}
