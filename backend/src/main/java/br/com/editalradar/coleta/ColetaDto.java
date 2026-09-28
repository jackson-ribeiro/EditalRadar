package br.com.editalradar.coleta;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public record ColetaDto(
        Long id,
        StatusColeta status,
        OrigemColeta origem,
        Instant iniciadaEm,
        Instant finalizadaEm,
        int totalListagem,
        int novos,
        int atualizados,
        int encerrados,
        int descartados,
        int detalhesBaixados,
        int pendentes,
        List<String> avisos,
        String mensagemErro
) {

    public static ColetaDto de(Coleta coleta) {
        List<String> avisos = coleta.getAvisos() == null
                ? List.of()
                : Arrays.stream(coleta.getAvisos().split("\n")).filter(aviso -> !aviso.isBlank()).toList();
        return new ColetaDto(coleta.getId(), coleta.getStatus(), coleta.getOrigem(), coleta.getIniciadaEm(),
                coleta.getFinalizadaEm(), coleta.getTotalListagem(), coleta.getNovos(), coleta.getAtualizados(),
                coleta.getEncerrados(), coleta.getDescartados(), coleta.getDetalhesBaixados(), coleta.getPendentes(),
                avisos, coleta.getMensagemErro());
    }
}
