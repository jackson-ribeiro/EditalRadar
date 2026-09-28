package br.com.editalradar.suporte;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public final class Fixtures {

    private Fixtures() {
    }

    public static String ler(String nome) {
        try (InputStream entrada = Fixtures.class.getResourceAsStream("/fixtures/" + nome)) {
            if (entrada == null) {
                throw new IllegalArgumentException("Fixture não encontrado: " + nome);
            }
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
