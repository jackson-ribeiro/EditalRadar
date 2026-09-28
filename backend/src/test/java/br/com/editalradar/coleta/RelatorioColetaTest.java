package br.com.editalradar.coleta;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RelatorioColetaTest {

    @Test
    void limitaAvisosERemoveQuebrasDeLinha() {
        RelatorioColeta relatorio = new RelatorioColeta();
        relatorio.avisar("linha 1\nlinha 2");
        for (int i = 0; i < 249; i++) {
            relatorio.avisar("aviso " + i);
        }

        String texto = relatorio.avisosComoTexto();
        String[] linhas = texto.split("\n");

        assertThat(linhas).hasSize(201);
        assertThat(linhas[0]).isEqualTo("linha 1 linha 2");
        assertThat(linhas[200]).isEqualTo("(+50 avisos omitidos)");
    }

    @Test
    void semAvisosRetornaNulo() {
        assertThat(new RelatorioColeta().avisosComoTexto()).isNull();
    }
}
