package br.com.editalradar.coleta;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SincronizacaoExecutorTest {

    private final SincronizacaoService servico = mock(SincronizacaoService.class);

    @Test
    void disparaColetaEmSegundoPlano() {
        Coleta coleta = mock(Coleta.class);
        when(coleta.getId()).thenReturn(7L);
        when(servico.iniciar(OrigemColeta.MANUAL)).thenReturn(coleta);
        SincronizacaoExecutor executor = new SincronizacaoExecutor(servico, Runnable::run);

        assertThat(executor.disparar(OrigemColeta.MANUAL)).contains(7L);
        verify(servico).executar(7L);
    }

    @Test
    void segundoDisparoEnquantoRodaEhRecusado() {
        Coleta coleta = mock(Coleta.class);
        when(coleta.getId()).thenReturn(7L);
        when(servico.iniciar(OrigemColeta.MANUAL)).thenReturn(coleta);
        List<Runnable> fila = new ArrayList<>();
        SincronizacaoExecutor executor = new SincronizacaoExecutor(servico, fila::add);

        assertThat(executor.disparar(OrigemColeta.MANUAL)).contains(7L);
        assertThat(executor.disparar(OrigemColeta.MANUAL)).isEmpty();
        verify(servico, times(1)).iniciar(OrigemColeta.MANUAL);

        fila.get(0).run();
        assertThat(executor.disparar(OrigemColeta.MANUAL)).contains(7L);
    }

    @Test
    void falhaAoIniciarLiberaNovaTentativa() {
        when(servico.iniciar(OrigemColeta.MANUAL)).thenThrow(new IllegalStateException("banco fora"));
        SincronizacaoExecutor executor = new SincronizacaoExecutor(servico, Runnable::run);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> executor.disparar(OrigemColeta.MANUAL))
                .isInstanceOf(IllegalStateException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> executor.disparar(OrigemColeta.MANUAL))
                .isInstanceOf(IllegalStateException.class);
        verify(servico, times(2)).iniciar(OrigemColeta.MANUAL);
    }
}
