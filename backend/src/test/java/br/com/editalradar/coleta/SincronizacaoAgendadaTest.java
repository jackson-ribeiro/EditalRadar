package br.com.editalradar.coleta;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SincronizacaoAgendadaTest {

    @Test
    void disparaColetaAgendada() {
        SincronizacaoExecutor executor = mock(SincronizacaoExecutor.class);
        when(executor.disparar(OrigemColeta.AGENDADA)).thenReturn(Optional.of(1L));

        new SincronizacaoAgendada(executor).executar();

        verify(executor).disparar(OrigemColeta.AGENDADA);
    }
}
