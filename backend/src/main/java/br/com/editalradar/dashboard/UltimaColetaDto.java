package br.com.editalradar.dashboard;

import br.com.editalradar.coleta.Coleta;
import br.com.editalradar.coleta.StatusColeta;

import java.time.Instant;

public record UltimaColetaDto(
        Long id,
        StatusColeta status,
        Instant iniciadaEm,
        Instant finalizadaEm,
        int novos,
        int atualizados,
        int encerrados
) {

    public static UltimaColetaDto de(Coleta coleta) {
        return new UltimaColetaDto(coleta.getId(), coleta.getStatus(), coleta.getIniciadaEm(),
                coleta.getFinalizadaEm(), coleta.getNovos(), coleta.getAtualizados(), coleta.getEncerrados());
    }
}
