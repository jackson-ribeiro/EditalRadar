package br.com.editalradar.scraper;

import br.com.editalradar.comum.Textos;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DatasTexto {

    public enum SituacaoPrazo {
        NORMAL,
        SUSPENSO,
        CANCELADO,
        DESCONHECIDO
    }

    public record Prazo(LocalDate inicio, LocalDate fim, SituacaoPrazo situacao) {
    }

    private static final Pattern INTERVALO = Pattern.compile(
            "(?<!\\d)(\\d{1,2})(?:/(\\d{1,2})(?:/(\\d{4}))?)?\\s+a\\s+(\\d{1,2})/(\\d{1,2})/(\\d{4})(?!\\d)");
    private static final Pattern DATA_NUMERICA = Pattern.compile("(?<!\\d)(\\d{1,2})/(\\d{1,2})/(\\d{4})(?!\\d)");
    private static final List<String> MESES = List.of(
            "janeiro", "fevereiro", "marco", "abril", "maio", "junho",
            "julho", "agosto", "setembro", "outubro", "novembro", "dezembro");
    private static final Pattern DATA_EXTENSO = Pattern.compile(
            "(?<!\\d)(\\d{1,2})[ºo°]?\\s+de\\s+(" + String.join("|", MESES) + ")(?:\\s+de\\s+(\\d{4}))?");

    private DatasTexto() {
    }

    public static Prazo interpretarPrazo(String texto) {
        String normalizado = Textos.normalizar(texto);
        if (normalizado.contains("cancelad")) {
            return new Prazo(null, null, SituacaoPrazo.CANCELADO);
        }
        if (normalizado.contains("suspens")) {
            return new Prazo(null, null, SituacaoPrazo.SUSPENSO);
        }
        try {
            Matcher intervalo = INTERVALO.matcher(normalizado);
            if (intervalo.find()) {
                LocalDate fim = LocalDate.of(numero(intervalo.group(6)), numero(intervalo.group(5)), numero(intervalo.group(4)));
                int mesInicio = intervalo.group(2) != null ? numero(intervalo.group(2)) : fim.getMonthValue();
                int anoInicio;
                if (intervalo.group(3) != null) {
                    anoInicio = numero(intervalo.group(3));
                } else {
                    anoInicio = mesInicio > fim.getMonthValue() ? fim.getYear() - 1 : fim.getYear();
                }
                LocalDate inicio = LocalDate.of(anoInicio, mesInicio, numero(intervalo.group(1)));
                return new Prazo(inicio, fim, SituacaoPrazo.NORMAL);
            }
            LocalDate ultima = null;
            Matcher numerica = DATA_NUMERICA.matcher(normalizado);
            while (numerica.find()) {
                ultima = LocalDate.of(numero(numerica.group(3)), numero(numerica.group(2)), numero(numerica.group(1)));
            }
            if (ultima != null) {
                return new Prazo(null, ultima, SituacaoPrazo.NORMAL);
            }
        } catch (DateTimeException e) {
            return new Prazo(null, null, SituacaoPrazo.DESCONHECIDO);
        }
        return new Prazo(null, null, SituacaoPrazo.DESCONHECIDO);
    }

    public static Optional<LocalDate> primeiraData(String frase) {
        return datas(frase).stream().findFirst();
    }

    public static List<LocalDate> datas(String frase) {
        String normalizado = Textos.normalizar(frase);
        List<Candidata> candidatas = new ArrayList<>();

        Matcher numerica = DATA_NUMERICA.matcher(normalizado);
        while (numerica.find()) {
            candidatas.add(new Candidata(numerica.start(), numero(numerica.group(1)), numero(numerica.group(2)),
                    numero(numerica.group(3))));
        }
        Matcher extenso = DATA_EXTENSO.matcher(normalizado);
        while (extenso.find()) {
            Integer ano = extenso.group(3) != null ? numero(extenso.group(3)) : null;
            candidatas.add(new Candidata(extenso.start(), numero(extenso.group(1)),
                    MESES.indexOf(extenso.group(2)) + 1, ano));
        }
        candidatas.sort(Comparator.comparingInt(Candidata::posicao));

        Integer anoSeguinte = null;
        for (int i = candidatas.size() - 1; i >= 0; i--) {
            Candidata candidata = candidatas.get(i);
            if (candidata.ano() == null) {
                candidatas.set(i, new Candidata(candidata.posicao(), candidata.dia(), candidata.mes(), anoSeguinte));
            } else {
                anoSeguinte = candidata.ano();
            }
        }

        List<LocalDate> datas = new ArrayList<>();
        for (Candidata candidata : candidatas) {
            if (candidata.ano() == null) {
                continue;
            }
            try {
                datas.add(LocalDate.of(candidata.ano(), candidata.mes(), candidata.dia()));
            } catch (DateTimeException e) {
                // data impossível (ex.: 31/02): ignorada
            }
        }
        return datas;
    }

    private static int numero(String texto) {
        return Integer.parseInt(texto);
    }

    private record Candidata(int posicao, int dia, int mes, Integer ano) {
    }
}
