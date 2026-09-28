package br.com.editalradar.comum;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Textos {

    private static final Pattern MARCAS = Pattern.compile("\\p{M}+");
    private static final Pattern ESPACOS = Pattern.compile("\\s+");

    private Textos() {
    }

    public static String limpar(String texto) {
        if (texto == null) {
            return "";
        }
        return ESPACOS.matcher(texto.replace(' ', ' ')).replaceAll(" ").strip();
    }

    public static String normalizar(String texto) {
        String decomposto = Normalizer.normalize(limpar(texto), Normalizer.Form.NFD);
        return MARCAS.matcher(decomposto).replaceAll("").toLowerCase(Locale.ROOT);
    }

    public static String vazioComoNulo(String texto) {
        String limpo = limpar(texto);
        return limpo.isEmpty() ? null : limpo;
    }
}
