package br.com.editalradar.dashboard;

public record ResumoDto(
        long abertos,
        long previstos,
        long encerrandoEm7Dias,
        long novosNaSemana,
        UltimaColetaDto ultimaColeta
) {
}
