package br.com.editalradar.comum;

import java.util.Locale;
import java.util.Set;

public final class Ufs {

    public static final Set<String> SIGLAS = Set.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA",
            "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    private Ufs() {
    }

    public static boolean valida(String sigla) {
        return sigla != null && SIGLAS.contains(sigla.strip().toUpperCase(Locale.ROOT));
    }
}
