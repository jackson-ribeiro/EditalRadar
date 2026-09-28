package br.com.editalradar.scraper;

import br.com.editalradar.comum.Textos;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PrevistosParser {

    private static final String BASE = "https://www.pciconcursos.com.br/";
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    public List<PrevistoListado> parse(String html) {
        Elements cabecalhos = Jsoup.parse(html, BASE).select("h2.principal");
        if (cabecalhos.isEmpty()) {
            throw new LayoutInesperadoException("Previstos sem h2.principal");
        }
        List<PrevistoListado> previstos = new ArrayList<>();
        for (Element cabecalho : cabecalhos) {
            String textoData = Textos.limpar(cabecalho.text());
            LocalDate data;
            try {
                data = LocalDate.parse(textoData, DATA);
            } catch (DateTimeParseException e) {
                throw new LayoutInesperadoException("Data inesperada em h2.principal: '" + textoData + "'");
            }
            Element lista = cabecalho.nextElementSibling();
            if (lista == null || !lista.is("ul.noticias")) {
                throw new LayoutInesperadoException("h2.principal '" + textoData + "' sem ul.noticias em seguida");
            }
            for (Element link : lista.select("li > a[href]")) {
                String titulo = Textos.vazioComoNulo(link.attr("title"));
                previstos.add(new PrevistoListado(link.absUrl("href"),
                        titulo != null ? titulo : Textos.limpar(link.text()), data));
            }
        }
        return List.copyOf(previstos);
    }
}
