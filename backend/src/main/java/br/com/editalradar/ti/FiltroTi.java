package br.com.editalradar.ti;

import br.com.editalradar.comum.Textos;
import br.com.editalradar.config.EditalRadarProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class FiltroTi {

    private final List<Pattern> palavrasChave;
    private final List<String> exclusoes;
    private final List<Pattern> termosEnsino;

    @Autowired
    public FiltroTi(EditalRadarProperties propriedades) {
        this(propriedades.ti().palavrasChave(), propriedades.ti().exclusoes(), propriedades.ti().termosEnsino());
    }

    public FiltroTi(List<String> palavrasChave, List<String> exclusoes) {
        this(palavrasChave, exclusoes, List.of());
    }

    public FiltroTi(List<String> palavrasChave, List<String> exclusoes, List<String> termosEnsino) {
        this.palavrasChave = padroesDePalavraInteira(palavrasChave);
        this.termosEnsino = padroesDePalavraInteira(termosEnsino);
        this.exclusoes = exclusoes.stream()
                .map(Textos::normalizar)
                .filter(exclusao -> !exclusao.isEmpty())
                .distinct()
                .toList();
    }

    public boolean ehTi(String... textos) {
        for (String texto : textos) {
            if (texto == null) {
                continue;
            }
            String textoNormalizado = Textos.normalizar(texto);
            if (termosEnsino.stream().anyMatch(termo -> termo.matcher(textoNormalizado).find())) {
                continue;
            }
            String normalizado = textoNormalizado;
            for (String exclusao : exclusoes) {
                normalizado = normalizado.replace(exclusao, " ");
            }
            for (Pattern palavra : palavrasChave) {
                if (palavra.matcher(normalizado).find()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<Pattern> padroesDePalavraInteira(List<String> termos) {
        return termos.stream()
                .map(Textos::normalizar)
                .filter(termo -> !termo.isEmpty())
                .distinct()
                .map(termo -> Pattern.compile("(?<![\\p{L}\\p{N}])" + Pattern.quote(termo) + "(?![\\p{L}\\p{N}])"))
                .toList();
    }

    public boolean ehTi(Collection<String> textos) {
        return ehTi(textos.toArray(String[]::new));
    }
}
