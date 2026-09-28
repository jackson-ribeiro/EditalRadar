package br.com.editalradar.scraper;

import br.com.editalradar.comum.Textos;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Component
public class DetalheParser {

    public static final int LIMITE_TRECHO = 600;

    private static final String BASE = "https://www.pciconcursos.com.br/";
    private static final Pattern FIM_DE_FRASE = Pattern.compile("(?<=[.!?])\\s+(?=\\p{Lu})");
    private static final Pattern PROVA = Pattern.compile("(?<![\\p{L}])provas?(?![\\p{L}])");
    private static final List<String> PALAVRAS_REMUNERACAO = List.of("remunera", "salario", "vencimento", "subsidio");

    public DetalheConcurso parse(String html) {
        Element corpo = Jsoup.parse(html, BASE).selectFirst("article#noticia [itemprop=articleBody]");
        if (corpo == null) {
            throw new LayoutInesperadoException("Detalhe sem article#noticia [itemprop=articleBody]");
        }

        List<String> cargos = corpo.select("ul > li").stream()
                .map(li -> Textos.limpar(li.text()))
                .filter(texto -> !texto.isEmpty())
                .toList();

        List<String> frases = corpo.select("p").stream()
                .flatMap(paragrafo -> Arrays.stream(FIM_DE_FRASE.split(Textos.limpar(paragrafo.text()))))
                .filter(frase -> !frase.isBlank())
                .toList();

        LocalDate inicioInscricao = primeiraDataEm(frases,
                frase -> frase.contains("inscri") && !frase.contains("isenc"));
        LocalDate dataProva = primeiraDataEm(frases,
                frase -> PROVA.matcher(frase).find() && !frase.contains("isenc"));

        String banca = corpo.select("a[href]").stream()
                .map(link -> link.absUrl("href"))
                .map(Bancas::deUrl)
                .flatMap(Optional::stream)
                .findFirst()
                .orElse(null);

        BigDecimal salarioMin = null;
        BigDecimal salarioMax = null;
        for (String frase : frases) {
            if (Textos.normalizar(frase).contains("varia de")) {
                List<BigDecimal> valores = ValoresTexto.valoresMonetarios(frase);
                if (valores.size() >= 2) {
                    salarioMin = Collections.min(valores);
                    salarioMax = Collections.max(valores);
                    break;
                }
            }
        }

        String trechoRemuneracao = trecho(frases.stream()
                .filter(frase -> frase.contains("R$"))
                .filter(frase -> {
                    String normalizada = Textos.normalizar(frase);
                    return !normalizada.contains("taxa") && !normalizada.contains("isenc") && !normalizada.contains("boleto")
                            && PALAVRAS_REMUNERACAO.stream().anyMatch(normalizada::contains);
                })
                .limit(2)
                .toList());
        List<String> frasesTaxa = frases.stream()
                .filter(frase -> frase.contains("R$") && Textos.normalizar(frase).contains("taxa"))
                .filter(frase -> !ehIsencaoPorRenda(Textos.normalizar(frase)))
                .limit(1)
                .toList();
        String trechoTaxa = trecho(frasesTaxa);
        BigDecimal taxaInscricao = null;
        if (!frasesTaxa.isEmpty()) {
            List<BigDecimal> valores = ValoresTexto.valoresMonetarios(frasesTaxa.get(0));
            if (valores.size() == 1) {
                taxaInscricao = valores.get(0);
            }
        }

        return new DetalheConcurso(cargos, inicioInscricao, dataProva, banca, salarioMin, salarioMax,
                trechoRemuneracao, trechoTaxa, taxaInscricao);
    }

    private static boolean ehIsencaoPorRenda(String fraseNormalizada) {
        return fraseNormalizada.contains("isenc")
                && (fraseNormalizada.contains("renda") || fraseNormalizada.contains("salario"));
    }

    private static String trecho(List<String> frases) {
        if (frases.isEmpty()) {
            return null;
        }
        String texto = String.join(" ", frases);
        if (texto.length() <= LIMITE_TRECHO) {
            return texto;
        }
        String corte = texto.substring(0, LIMITE_TRECHO - 1);
        int ultimoEspaco = corte.lastIndexOf(' ');
        if (ultimoEspaco > LIMITE_TRECHO / 2) {
            corte = corte.substring(0, ultimoEspaco);
        }
        return corte.stripTrailing() + "…";
    }

    private static LocalDate primeiraDataEm(List<String> frases, Predicate<String> criterioNormalizado) {
        for (String frase : frases) {
            if (criterioNormalizado.test(Textos.normalizar(frase))) {
                Optional<LocalDate> data = DatasTexto.primeiraData(frase);
                if (data.isPresent()) {
                    return data.get();
                }
            }
        }
        return null;
    }
}
