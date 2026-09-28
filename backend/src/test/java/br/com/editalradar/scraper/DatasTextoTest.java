package br.com.editalradar.scraper;

import br.com.editalradar.scraper.DatasTexto.SituacaoPrazo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DatasTextoTest {

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            16/10/2026                        |            | 2026-10-16 | NORMAL
            Reaberto até 25/09/2026           |            | 2026-09-25 | NORMAL
            Prorrogado até 21/09/2026         |            | 2026-09-21 | NORMAL
            26/09 a 05/10/2026                | 2026-09-26 | 2026-10-05 | NORMAL
            10 a 13/10/2026                   | 2026-10-10 | 2026-10-13 | NORMAL
            Reabertura de 27/10 a 26/11/2026  | 2026-10-27 | 2026-11-26 | NORMAL
            14/12/2026 a 04/01/2027           | 2026-12-14 | 2027-01-04 | NORMAL
            20/12 a 10/01/2027                | 2026-12-20 | 2027-01-10 | NORMAL
            Suspenso                          |            |            | SUSPENSO
            Cancelado                         |            |            | CANCELADO
            Consulte o edital                 |            |            | DESCONHECIDO
            31/02/2026                        |            |            | DESCONHECIDO
            """)
    void interpretaPrazoDaListagem(String texto, LocalDate inicio, LocalDate fim, SituacaoPrazo situacao) {
        DatasTexto.Prazo prazo = DatasTexto.interpretarPrazo(texto);

        assertThat(prazo.inicio()).isEqualTo(inicio);
        assertThat(prazo.fim()).isEqualTo(fim);
        assertThat(prazo.situacao()).isEqualTo(situacao);
    }

    @Test
    void extraiDatasPorExtensoENumericasNaOrdem() {
        assertThat(DatasTexto.datas("entre 21 de setembro de 2026, às 10h, e 21 de outubro de 2026, às 23h"))
                .containsExactly(LocalDate.of(2026, 9, 21), LocalDate.of(2026, 10, 21));
        assertThat(DatasTexto.datas("das 16h do dia 30 de outubro de 2026 até às 16h do dia 1º de dezembro de 2026"))
                .containsExactly(LocalDate.of(2026, 10, 30), LocalDate.of(2026, 12, 1));
        assertThat(DatasTexto.datas("prova em 10/01/2027 e resultado em 5 de fevereiro de 2027"))
                .containsExactly(LocalDate.of(2027, 1, 10), LocalDate.of(2027, 2, 5));
    }

    @Test
    void dataSemAnoHerdaAnoDaProximaData() {
        assertThat(DatasTexto.datas("entre 5 de março e 20 de abril de 2027"))
                .containsExactly(LocalDate.of(2027, 3, 5), LocalDate.of(2027, 4, 20));
    }

    @Test
    void semDataResolvivelRetornaVazio() {
        assertThat(DatasTexto.datas("sem datas aqui")).isEmpty();
        assertThat(DatasTexto.datas("previsto para 3 de maio")).isEmpty();
        assertThat(DatasTexto.primeiraData(null)).isEmpty();
        assertThat(DatasTexto.primeiraData("em 29 de novembro de 2026")).contains(LocalDate.of(2026, 11, 29));
    }
}
