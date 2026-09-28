package br.com.editalradar.concurso;

import br.com.editalradar.web.ParametroInvalidoException;

import java.util.Locale;
import java.util.Set;

public record OrdenacaoConcurso(String campo, boolean crescente) {

    private static final Set<String> CAMPOS = Set.of("fimInscricao", "salarioMax", "primeiraVezVistoEm");

    public static OrdenacaoConcurso de(String texto) {
        String valor = texto == null || texto.isBlank() ? "fimInscricao,asc" : texto.strip();
        String[] partes = valor.split(",");
        String campo = partes[0].strip();
        if (partes.length > 2 || !CAMPOS.contains(campo)) {
            throw invalida(texto);
        }
        boolean crescente = true;
        if (partes.length == 2) {
            String direcao = partes[1].strip().toLowerCase(Locale.ROOT);
            if (direcao.equals("desc")) {
                crescente = false;
            } else if (!direcao.equals("asc")) {
                throw invalida(texto);
            }
        }
        return new OrdenacaoConcurso(campo, crescente);
    }

    private static ParametroInvalidoException invalida(String texto) {
        return new ParametroInvalidoException("Ordenação inválida: '" + texto
                + "'. Use fimInscricao, salarioMax ou primeiraVezVistoEm, com ,asc ou ,desc.");
    }
}
