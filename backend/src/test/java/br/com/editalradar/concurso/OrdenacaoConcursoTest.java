package br.com.editalradar.concurso;

import br.com.editalradar.web.ParametroInvalidoException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrdenacaoConcursoTest {

    @Test
    void interpretaOrdenacoesValidas() {
        assertThat(OrdenacaoConcurso.de(null)).isEqualTo(new OrdenacaoConcurso("fimInscricao", true));
        assertThat(OrdenacaoConcurso.de("salarioMax,desc")).isEqualTo(new OrdenacaoConcurso("salarioMax", false));
        assertThat(OrdenacaoConcurso.de("primeiraVezVistoEm")).isEqualTo(new OrdenacaoConcurso("primeiraVezVistoEm", true));
        assertThat(OrdenacaoConcurso.de(" fimInscricao , ASC ")).isEqualTo(new OrdenacaoConcurso("fimInscricao", true));
    }

    @Test
    void rejeitaCampoOuDirecaoInvalidos() {
        assertThatThrownBy(() -> OrdenacaoConcurso.de("orgao,asc")).isInstanceOf(ParametroInvalidoException.class);
        assertThatThrownBy(() -> OrdenacaoConcurso.de("salarioMax,para-cima")).isInstanceOf(ParametroInvalidoException.class);
        assertThatThrownBy(() -> OrdenacaoConcurso.de("salarioMax,asc,x")).isInstanceOf(ParametroInvalidoException.class);
    }
}
