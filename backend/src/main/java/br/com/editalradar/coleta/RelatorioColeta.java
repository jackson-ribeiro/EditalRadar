package br.com.editalradar.coleta;

import java.util.ArrayList;
import java.util.List;

final class RelatorioColeta {

    private static final int MAX_AVISOS = 200;

    int totalListagem;
    int novos;
    int atualizados;
    int encerrados;
    int descartados;
    int detalhesBaixados;
    int pendentes;

    private final List<String> avisos = new ArrayList<>();
    private int avisosOmitidos;

    void avisar(String aviso) {
        if (avisos.size() < MAX_AVISOS) {
            avisos.add(aviso.replaceAll("\\R", " "));
        } else {
            avisosOmitidos++;
        }
    }

    String avisosComoTexto() {
        if (avisos.isEmpty()) {
            return null;
        }
        List<String> linhas = new ArrayList<>(avisos);
        if (avisosOmitidos > 0) {
            linhas.add("(+" + avisosOmitidos + " avisos omitidos)");
        }
        return String.join("\n", linhas);
    }
}
