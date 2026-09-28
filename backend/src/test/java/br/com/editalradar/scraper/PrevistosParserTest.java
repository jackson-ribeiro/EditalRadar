package br.com.editalradar.scraper;

import br.com.editalradar.suporte.Fixtures;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrevistosParserTest {

    private final PrevistosParser parser = new PrevistosParser();

    @Test
    void lePrimeiraPaginaDePrevistos() {
        List<PrevistoListado> previstos = parser.parse(Fixtures.ler("previstos.html"));

        assertThat(previstos).hasSize(150);
        assertThat(previstos.get(0)).isEqualTo(new PrevistoListado(
                "https://www.pciconcursos.com.br/previstos/concurso-bacen-deve-ser-autorizado-com-140-vagas-120-para-auditor-e-20-para-procurador",
                "Concurso Bacen deve ser autorizado com 140 vagas; 120 para auditor e 20 para procurador",
                LocalDate.of(2026, 6, 23)));
        assertThat(previstos.get(149).dataPublicacao()).isEqualTo(LocalDate.of(2026, 1, 16));
        assertThat(previstos).allSatisfy(previsto ->
                assertThat(previsto.url()).startsWith("https://www.pciconcursos.com.br/previstos/"));
    }

    @Test
    void semCabecalhosLancaExcecao() {
        assertThatThrownBy(() -> parser.parse("<html><body><p>vazio</p></body></html>"))
                .isInstanceOf(LayoutInesperadoException.class)
                .hasMessageContaining("h2.principal");
    }
}
