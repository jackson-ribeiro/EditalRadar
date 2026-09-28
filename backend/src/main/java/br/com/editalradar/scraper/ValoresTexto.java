package br.com.editalradar.scraper;

import br.com.editalradar.comum.Textos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ValoresTexto {

    public record VagasSalario(Integer vagas, boolean cadastroReserva, BigDecimal salarioMin, BigDecimal salarioMax) {
    }

    private static final Pattern VAGAS = Pattern.compile("(\\d[\\d.]*)\\s+vagas?(?![\\p{L}])");
    private static final Pattern CR = Pattern.compile("(?<![\\p{L}\\p{N}])cr(?![\\p{L}\\p{N}])");
    private static final Pattern ATE = Pattern.compile("(?<![\\p{L}])ate(?![\\p{L}])");
    private static final Pattern DINHEIRO = Pattern.compile(
            "R\\$\\s*(\\d{1,3}(?:\\.\\d{3})+(?:,\\d{1,2})?|\\d+(?:,\\d{1,2})?)", Pattern.CASE_INSENSITIVE);

    private ValoresTexto() {
    }

    public static VagasSalario interpretarVagasSalario(String linha) {
        String texto = Textos.limpar(linha);
        String normalizado = Textos.normalizar(texto);

        Integer vagas = null;
        Matcher vagasEncontradas = VAGAS.matcher(normalizado);
        if (vagasEncontradas.find()) {
            vagas = inteiro(vagasEncontradas.group(1));
        }

        boolean cadastroReserva = normalizado.contains("cadastro") || CR.matcher(normalizado).find();

        BigDecimal salarioMin = null;
        BigDecimal salarioMax = null;
        if (!normalizado.contains("hora")) {
            List<BigDecimal> valores = valoresMonetarios(texto);
            if (valores.size() >= 2) {
                salarioMin = Collections.min(valores);
                salarioMax = Collections.max(valores);
            } else if (valores.size() == 1) {
                salarioMax = valores.get(0);
                if (!ATE.matcher(normalizado).find()) {
                    salarioMin = salarioMax;
                }
            }
        }
        return new VagasSalario(vagas, cadastroReserva, salarioMin, salarioMax);
    }

    public static List<BigDecimal> valoresMonetarios(String texto) {
        List<BigDecimal> valores = new ArrayList<>();
        Matcher encontrado = DINHEIRO.matcher(Textos.limpar(texto));
        while (encontrado.find()) {
            valores.add(decimal(encontrado.group(1)));
        }
        return valores;
    }

    private static BigDecimal decimal(String numero) {
        return new BigDecimal(numero.replace(".", "").replace(',', '.')).setScale(2, RoundingMode.UNNECESSARY);
    }

    private static Integer inteiro(String numero) {
        try {
            return Integer.valueOf(numero.replace(".", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
