package br.com.editalradar.scraper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ValoresTextoTest {

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            15 vagas até R$ 7.181,50                      | 15  | false |          | 7181.50
            171 vagas                                     | 171 | false |          |
            Cadastro de reserva até R$ 8.800,00           |     | true  |          | 8800.00
            Vagas até R$ 7.647,20                         |     | false |          | 7647.20
            1 vaga até R$ 4.586,83                        | 1   | false |          | 4586.83
            4 vagas e CR até R$ 7.114,33                  | 4   | true  |          | 7114.33
            146 vagas + 290 CR até R$ 18.000,00           | 146 | true  |          | 18000.00
            Até R$ 4.301,84                               |     | false |          | 4301.84
            Cadastro Reserva até R$ 133,28 por hora       |     | true  |          |
            46 vagas R$ 30,00 por hora-aula               | 46  | false |          |
            1 vaga + CR até R$ 3242,00 nacionais          | 1   | true  |          | 3242.00
            9 vagas + CR                                  | 9   | true  |          |
            1.496 vagas até R$ 9.000,00                   | 1496| false |          | 9000.00
            de R$ 2.698,73 a R$ 13.305,57                 |     | false | 2698.73  | 13305.57
            2 vagas com salário de R$ 5.000,00            | 2   | false | 5000.00  | 5000.00
            """)
    void interpretaLinhaDeVagasESalario(String linha, Integer vagas, boolean cadastroReserva,
                                        BigDecimal salarioMin, BigDecimal salarioMax) {
        ValoresTexto.VagasSalario resultado = ValoresTexto.interpretarVagasSalario(linha);

        assertThat(resultado.vagas()).isEqualTo(vagas);
        assertThat(resultado.cadastroReserva()).isEqualTo(cadastroReserva);
        assertDecimal(resultado.salarioMin(), salarioMin);
        assertDecimal(resultado.salarioMax(), salarioMax);
    }

    @Test
    void extraiValoresMonetariosNaOrdem() {
        assertThat(ValoresTexto.valoresMonetarios("de R$ 85,00 e de R$ 1.100,5; sem valor R$ abc"))
                .containsExactly(new BigDecimal("85.00"), new BigDecimal("1100.50"));
    }

    @Test
    void linhaVaziaNaoTemDados() {
        ValoresTexto.VagasSalario resultado = ValoresTexto.interpretarVagasSalario(null);
        assertThat(resultado.vagas()).isNull();
        assertThat(resultado.cadastroReserva()).isFalse();
        assertThat(resultado.salarioMin()).isNull();
        assertThat(resultado.salarioMax()).isNull();
    }

    private static void assertDecimal(BigDecimal atual, BigDecimal esperado) {
        if (esperado == null) {
            assertThat(atual).isNull();
        } else {
            assertThat(atual).isEqualByComparingTo(esperado);
        }
    }
}
