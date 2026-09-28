package br.com.editalradar.comum;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextosTest {

    @Test
    void limparTrocaNbspEColapsaEspacos() {
        assertThat(Textos.limpar("  Analista \n  de   TI ")).isEqualTo("Analista de TI");
        assertThat(Textos.limpar(null)).isEmpty();
    }

    @Test
    void normalizarRemoveAcentosEMaiusculas() {
        assertThat(Textos.normalizar("Tecnologia da INFORMAÇÃO – Técnico")).isEqualTo("tecnologia da informacao – tecnico");
    }

    @Test
    void vazioComoNulo() {
        assertThat(Textos.vazioComoNulo(" ")).isNull();
        assertThat(Textos.vazioComoNulo(" SP ")).isEqualTo("SP");
    }

    @Test
    void ufsValidas() {
        assertThat(Ufs.valida("sp")).isTrue();
        assertThat(Ufs.valida("DF")).isTrue();
        assertThat(Ufs.valida("XX")).isFalse();
        assertThat(Ufs.valida(null)).isFalse();
        assertThat(Ufs.SIGLAS).hasSize(27);
    }
}
