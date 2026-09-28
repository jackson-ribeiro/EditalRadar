package br.com.editalradar.concurso;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConcursoSpecificationsTest {

    @Test
    void escapaCuringasDoLike() {
        assertThat(ConcursoSpecifications.escaparLike("100%_ti\\x")).isEqualTo("100\\%\\_ti\\\\x");
    }
}
