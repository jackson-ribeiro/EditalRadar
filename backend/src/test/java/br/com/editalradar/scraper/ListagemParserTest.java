package br.com.editalradar.scraper;

import br.com.editalradar.scraper.DatasTexto.SituacaoPrazo;
import br.com.editalradar.suporte.Fixtures;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ListagemParserTest {

    private static final ListagemParser PARSER = new ListagemParser();
    private static ResultadoListagem abertos;

    @BeforeAll
    static void carregar() {
        abertos = PARSER.parse(Fixtures.ler("concursos-abertos.html"));
    }

    @Test
    void encontraTodosOsItensSemAvisos() {
        assertThat(abertos.itens()).hasSize(457);
        assertThat(abertos.avisos()).isEmpty();
        assertThat(abertos.itens()).allSatisfy(item ->
                assertThat(item.urlOrigem()).startsWith("https://www.pciconcursos.com.br/noticias/"));
        assertThat(abertos.itens().stream().filter(ConcursoListado::nacional)).hasSize(9);
        assertThat(abertos.itens().stream().filter(ConcursoListado::variosCargos)).hasSize(259);
    }

    @Test
    void interpretaItemNacional() {
        ConcursoListado crbio = item("crbio-01-sp-mt-e-ms-abre-concurso-publico");

        assertThat(crbio.orgao()).isEqualTo("CRBio-01 - Conselho Regional de Biologia da 1ª Região");
        assertThat(crbio.titulo()).isEqualTo("CRBio-01 - SP, MT e MS abre concurso público com salários de até R$ 7.181,50");
        assertThat(crbio.nacional()).isTrue();
        assertThat(crbio.uf()).isNull();
        assertThat(crbio.vagas()).isEqualTo(15);
        assertThat(crbio.salarioMin()).isNull();
        assertThat(crbio.salarioMax()).isEqualByComparingTo(new BigDecimal("7181.50"));
        assertThat(crbio.cargo()).isEqualTo("Vários Cargos");
        assertThat(crbio.variosCargos()).isTrue();
        assertThat(crbio.escolaridade()).isEqualTo("Médio / Superior");
        assertThat(crbio.inicioInscricao()).isNull();
        assertThat(crbio.fimInscricao()).isEqualTo(LocalDate.of(2026, 10, 21));
        assertThat(crbio.situacaoPrazo()).isEqualTo(SituacaoPrazo.NORMAL);
    }

    @Test
    void interpretaItemEstadualComIntervalo() {
        ConcursoListado unai = item("camara-de-unai-mg-abre-concurso-publico");

        assertThat(unai.uf()).isEqualTo("MG");
        assertThat(unai.nacional()).isFalse();
        assertThat(unai.vagas()).isEqualTo(2);
        assertThat(unai.salarioMax()).isEqualByComparingTo(new BigDecimal("9193.89"));
        assertThat(unai.cargo()).isEqualTo("Analista de Atividades da Secretaria");
        assertThat(unai.variosCargos()).isFalse();
        assertThat(unai.escolaridade()).isEqualTo("Superior");
        assertThat(unai.inicioInscricao()).isEqualTo(LocalDate.of(2026, 10, 30));
        assertThat(unai.fimInscricao()).isEqualTo(LocalDate.of(2026, 12, 1));
    }

    @Test
    void interpretaVariantesDeDestaque() {
        ConcursoListado andradina = item("prefeitura-de-andradina-sp-retifica-concurso-publico");
        assertThat(andradina.uf()).isEqualTo("SP");
        assertThat(andradina.vagas()).isEqualTo(19);
        assertThat(andradina.salarioMax()).isEqualByComparingTo(new BigDecimal("7341.23"));

        ConcursoListado transpetro = item("transpetro-publica-retificacoes");
        assertThat(transpetro.nacional()).isTrue();
        assertThat(transpetro.vagas()).isNull();
        assertThat(transpetro.salarioMax()).isEqualByComparingTo(new BigDecimal("15034.81"));
        assertThat(transpetro.fimInscricao()).isEqualTo(LocalDate.of(2026, 9, 21));
    }

    @Test
    void reconheceSuspensoECancelado() {
        assertThat(item("epagri-sc-suspende-temporariamente-concurso-publico").situacaoPrazo())
                .isEqualTo(SituacaoPrazo.SUSPENSO);
        assertThat(item("camara-de-mirassol-doeste-mt-cancela-concurso-publico").situacaoPrazo())
                .isEqualTo(SituacaoPrazo.CANCELADO);
    }

    @Test
    void paginaNacionalTemOitoItens() {
        ResultadoListagem nacional = PARSER.parse(Fixtures.ler("concursos-nacional.html"));
        assertThat(nacional.itens()).hasSize(8);
        assertThat(nacional.itens()).allSatisfy(item -> assertThat(item.nacional()).isTrue());
    }

    @Test
    void itemComLayoutParcialGeraAvisoSemQuebrar() {
        String html = """
                <html><body><div id="concursos">
                  <div class="na" data-url="https://www.pciconcursos.com.br/noticias/parcial">
                    <div class="ca"><a href="#" title="Manchete">Órgão Parcial</a></div>
                    <div class="cc">XX</div>
                  </div>
                </div></body></html>
                """;

        ResultadoListagem resultado = PARSER.parse(html);

        assertThat(resultado.itens()).hasSize(1);
        ConcursoListado parcial = resultado.itens().get(0);
        assertThat(parcial.orgao()).isEqualTo("Órgão Parcial");
        assertThat(parcial.uf()).isNull();
        assertThat(parcial.nacional()).isFalse();
        assertThat(parcial.cargo()).isNull();
        assertThat(parcial.fimInscricao()).isNull();
        assertThat(parcial.situacaoPrazo()).isEqualTo(SituacaoPrazo.DESCONHECIDO);
        assertThat(resultado.avisos()).anySatisfy(aviso -> assertThat(aviso).contains("UF desconhecida 'XX'"));
        assertThat(resultado.avisos()).anySatisfy(aviso -> assertThat(aviso).contains("Prazo não reconhecido"));
        assertThat(resultado.avisos()).anySatisfy(aviso -> assertThat(aviso).contains("sem .cd"));
    }

    @Test
    void itemSemCelulaDeUfNaoViraNacional() {
        String html = """
                <html><body><div id="concursos">
                  <div class="na" data-url="https://www.pciconcursos.com.br/noticias/sem-uf">
                    <div class="ca"><a href="#" title="Manchete">Órgão sem UF</a></div>
                    <div class="cd">1 vaga<br><span>Analista de TI<br><span>Superior</span></span></div>
                    <div class="ce"><span>10/10/2026</span></div>
                  </div>
                </div></body></html>
                """;

        ResultadoListagem resultado = PARSER.parse(html);

        ConcursoListado item = resultado.itens().get(0);
        assertThat(item.nacional()).isFalse();
        assertThat(item.uf()).isNull();
        assertThat(resultado.avisos()).anySatisfy(aviso -> assertThat(aviso).contains("sem .cc"));
    }

    @Test
    void layoutSemContainerLancaExcecao() {
        assertThatThrownBy(() -> PARSER.parse("<html><body><p>Manutenção</p></body></html>"))
                .isInstanceOf(LayoutInesperadoException.class)
                .hasMessageContaining("#concursos");
        assertThatThrownBy(() -> PARSER.parse("<html><body><div id=\"concursos\"></div></body></html>"))
                .isInstanceOf(LayoutInesperadoException.class)
                .hasMessageContaining("div[data-url]");
    }

    private static ConcursoListado item(String trechoUrl) {
        return abertos.itens().stream()
                .filter(item -> item.urlOrigem().contains(trechoUrl))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Item não encontrado no fixture: " + trechoUrl));
    }
}
