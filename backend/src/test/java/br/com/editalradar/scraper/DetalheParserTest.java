package br.com.editalradar.scraper;

import br.com.editalradar.suporte.Fixtures;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DetalheParserTest {

    private final DetalheParser parser = new DetalheParser();

    @Test
    void crbio01() {
        DetalheConcurso detalhe = parser.parse(Fixtures.ler("detalhe-crbio01.html"));

        assertThat(detalhe.banca()).isEqualTo("Quadrix");
        assertThat(detalhe.inicioInscricao()).isEqualTo(LocalDate.of(2026, 9, 21));
        assertThat(detalhe.dataProva()).isEqualTo(LocalDate.of(2026, 11, 29));
        assertThat(detalhe.cargos()).hasSize(12)
                .contains("Analista de Tecnologia da Informação (São Paulo - cadastro de reserva)");
        assertThat(detalhe.salarioMin()).isNull();
        assertThat(detalhe.salarioMax()).isNull();
    }

    @Test
    void camaraDeUnai() {
        DetalheConcurso detalhe = parser.parse(Fixtures.ler("detalhe-camara-unai.html"));

        assertThat(detalhe.banca()).isEqualTo("Consulplan");
        assertThat(detalhe.inicioInscricao()).isEqualTo(LocalDate.of(2026, 10, 30));
        assertThat(detalhe.dataProva()).isEqualTo(LocalDate.of(2027, 1, 10));
        assertThat(detalhe.cargos()).hasSize(3)
                .first().asString().contains("Analista de Sistemas");
    }

    @Test
    void prefeituraDeContagem() {
        DetalheConcurso detalhe = parser.parse(Fixtures.ler("detalhe-prefeitura-contagem.html"));

        assertThat(detalhe.banca()).isEqualTo("IBGP");
        assertThat(detalhe.inicioInscricao()).isEqualTo(LocalDate.of(2026, 10, 19));
        assertThat(detalhe.dataProva()).isEqualTo(LocalDate.of(2027, 1, 24));
        assertThat(detalhe.cargos()).hasSize(36).contains("Analista de TI (1 vaga)");
        assertThat(detalhe.salarioMin()).isEqualByComparingTo(new BigDecimal("2698.73"));
        assertThat(detalhe.salarioMax()).isEqualByComparingTo(new BigDecimal("13305.57"));
    }

    @Test
    void dpePbTemTrechosPorGrupoENivelSemTaxaUnica() {
        DetalheConcurso detalhe = parser.parse(Fixtures.ler("detalhe-dpe-pb.html"));

        assertThat(detalhe.trechoRemuneracao())
                .isEqualTo("A remuneração para Técnico é de R$ 4.042,50 e para Analista é de R$ 5.197,50.");
        assertThat(detalhe.trechoTaxa()).isEqualTo(
                "As taxas de inscrição são de R$ 103,00 para cargos de nível médio e de R$ 123,00 para cargos de nível superior.");
        assertThat(detalhe.taxaInscricao()).isNull();
        assertThat(detalhe.cargos()).contains("Analista da Defensoria - Desenvolvimento de Sistemas (1 vaga)");
    }

    @Test
    void funcampTemSalarioETaxaUnica() {
        DetalheConcurso detalhe = parser.parse(Fixtures.ler("detalhe-funcamp-analista.html"));

        assertThat(detalhe.trechoRemuneracao()).isEqualTo("O salário é de R$ 5.100,00 por mês.");
        assertThat(detalhe.trechoTaxa())
                .isEqualTo("A taxa de inscrição é de R$ 90,00 e não haverá isenção, salvo cancelamento do processo pela FUNCAMP.");
        assertThat(detalhe.taxaInscricao()).isEqualByComparingTo(new BigDecimal("90.00"));
    }

    @Test
    void unaiTemTaxaUnicaParaTodosOsCargos() {
        DetalheConcurso detalhe = parser.parse(Fixtures.ler("detalhe-camara-unai.html"));

        assertThat(detalhe.trechoRemuneracao()).startsWith("A remuneração é de R$ 9.193,89, acrescida de auxílio-alimentação de R$ 1.200,00");
        assertThat(detalhe.trechoTaxa()).isEqualTo("A taxa de inscrição é de R$ 80,00 para todos os cargos.");
        assertThat(detalhe.taxaInscricao()).isEqualByComparingTo(new BigDecimal("80.00"));
    }

    @Test
    void crbioTemTaxaPorNivel() {
        DetalheConcurso detalhe = parser.parse(Fixtures.ler("detalhe-crbio01.html"));

        assertThat(detalhe.trechoRemuneracao()).startsWith("As remunerações são de R$ 3.399,52 para cargos de nível médio");
        assertThat(detalhe.trechoTaxa()).startsWith("A taxa de inscrição é de R$ 85,00 para cargos de nível médio");
        assertThat(detalhe.taxaInscricao()).isNull();
    }

    @Test
    void frasesSemValorNaoViramTrecho() {
        String html = """
                <html><body><article id="noticia"><div itemprop="articleBody">
                  <p>Haverá isenção da taxa conforme o edital. A remuneração será divulgada depois.</p>
                </div></article></body></html>
                """;

        DetalheConcurso detalhe = parser.parse(html);

        assertThat(detalhe.trechoTaxa()).isNull();
        assertThat(detalhe.trechoRemuneracao()).isNull();
        assertThat(detalhe.taxaInscricao()).isNull();
    }

    @Test
    void isencaoPorRendaNaoViraTaxaNemSalario() {
        String html = """
                <html><body><article id="noticia"><div itemprop="articleBody">
                  <p>Candidatos com renda familiar de até R$ 2.277,00 podem pedir isenção da taxa. A taxa varia conforme o cargo.</p>
                  <p>Quem ganha até um salário mínimo (R$ 1.518,00) tem direito à isenção. O boleto de R$ 90,00 deve ser pago até o vencimento.</p>
                  <p>O salário é de R$ 5.100,00 por mês.</p>
                </div></article></body></html>
                """;

        DetalheConcurso detalhe = parser.parse(html);

        assertThat(detalhe.taxaInscricao()).isNull();
        assertThat(detalhe.trechoTaxa()).isNull();
        assertThat(detalhe.trechoRemuneracao()).isEqualTo("O salário é de R$ 5.100,00 por mês.");
    }

    @Test
    void trechoLongoECortadoEm600Caracteres() {
        String longo = "A remuneração é de R$ 5.000,00 " + "e inclui beneficios diversos ".repeat(40) + "no total.";
        String html = "<html><body><article id=\"noticia\"><div itemprop=\"articleBody\"><p>" + longo
                + "</p></div></article></body></html>";

        DetalheConcurso detalhe = parser.parse(html);

        assertThat(detalhe.trechoRemuneracao())
                .hasSizeLessThanOrEqualTo(DetalheParser.LIMITE_TRECHO)
                .endsWith("…")
                .matches("(?s).*(?:\\se|inclui|beneficios|diversos)…");
    }

    @Test
    void taxaEmValorUsaAFraseInteiraMesmoQuandoOTrechoECortado() {
        String longa = "A taxa de inscrição, " + "conforme as regras gerais do edital publicado ".repeat(15)
                + "é de R$ 123,00.";
        String html = "<html><body><article id=\"noticia\"><div itemprop=\"articleBody\"><p>" + longa
                + "</p></div></article></body></html>";

        DetalheConcurso detalhe = parser.parse(html);

        assertThat(detalhe.trechoTaxa()).hasSizeLessThanOrEqualTo(DetalheParser.LIMITE_TRECHO).endsWith("…");
        assertThat(detalhe.taxaInscricao()).isEqualByComparingTo("123.00");
    }

    @Test
    void artigoSemDadosRetornaCamposVazios() {
        String html = """
                <html><body><article id="noticia"><div itemprop="articleBody">
                  <p>Edital publicado. Mais informações em breve.</p>
                  <p>Veja o <a href="https://www.prefeitura.sp.gov.br/">site da prefeitura</a>.</p>
                </div></article></body></html>
                """;

        DetalheConcurso detalhe = parser.parse(html);

        assertThat(detalhe.cargos()).isEmpty();
        assertThat(detalhe.inicioInscricao()).isNull();
        assertThat(detalhe.dataProva()).isNull();
        assertThat(detalhe.banca()).isNull();
        assertThat(detalhe.salarioMin()).isNull();
        assertThat(detalhe.trechoRemuneracao()).isNull();
        assertThat(detalhe.trechoTaxa()).isNull();
        assertThat(detalhe.taxaInscricao()).isNull();
    }

    @Test
    void semCorpoDoArtigoLancaExcecao() {
        assertThatThrownBy(() -> parser.parse("<html><body><h1>Página não encontrada</h1></body></html>"))
                .isInstanceOf(LayoutInesperadoException.class)
                .hasMessageContaining("articleBody");
    }
}
