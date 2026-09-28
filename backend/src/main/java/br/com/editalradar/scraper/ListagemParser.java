package br.com.editalradar.scraper;

import br.com.editalradar.comum.Textos;
import br.com.editalradar.comum.Ufs;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class ListagemParser {

    private static final String BASE = "https://www.pciconcursos.com.br/";

    public ResultadoListagem parse(String html) {
        Document documento = Jsoup.parse(html, BASE);
        Element container = documento.getElementById("concursos");
        if (container == null) {
            throw new LayoutInesperadoException("Listagem sem o elemento #concursos");
        }
        Elements elementos = container.select("div[data-url]");
        if (elementos.isEmpty()) {
            throw new LayoutInesperadoException("Listagem sem itens div[data-url] dentro de #concursos");
        }

        List<String> avisos = new ArrayList<>();
        Map<String, ConcursoListado> itens = new LinkedHashMap<>();
        for (Element elemento : elementos) {
            ConcursoListado item = interpretar(elemento, avisos);
            if (item != null && itens.putIfAbsent(item.urlOrigem(), item) != null) {
                avisos.add("URL repetida na listagem: " + item.urlOrigem());
            }
        }
        if (itens.isEmpty()) {
            throw new LayoutInesperadoException("Nenhum item da listagem pôde ser interpretado");
        }
        return new ResultadoListagem(new ArrayList<>(itens.values()), avisos);
    }

    private ConcursoListado interpretar(Element elemento, List<String> avisos) {
        String url = elemento.absUrl("data-url");
        Element link = elemento.selectFirst(".ca > a");
        if (url.isEmpty() || link == null) {
            avisos.add("Item da listagem sem URL ou sem .ca > a: " + Textos.limpar(elemento.text()));
            return null;
        }

        String orgao = Textos.limpar(link.text());
        String titulo = Textos.vazioComoNulo(link.attr("title"));

        Element celulaUf = elemento.selectFirst(".cc");
        String textoUf = celulaUf == null ? "" : Textos.limpar(celulaUf.text());
        String uf = null;
        boolean nacional = false;
        if (celulaUf == null) {
            avisos.add("Item da listagem sem .cc (UF) em " + url);
        } else if (textoUf.isEmpty()) {
            nacional = true;
        } else if (Ufs.valida(textoUf)) {
            uf = textoUf.toUpperCase(Locale.ROOT);
        } else {
            avisos.add("UF desconhecida '" + textoUf + "' em " + url);
        }

        Element celulaDados = elemento.selectFirst(".cd");
        if (celulaDados == null) {
            avisos.add("Item da listagem sem .cd (vagas/cargo) em " + url);
        }
        ValoresTexto.VagasSalario vagasSalario =
                ValoresTexto.interpretarVagasSalario(celulaDados == null ? "" : celulaDados.ownText());
        Element spanCargo = celulaDados == null ? null : primeiroFilho(celulaDados, "span");
        String cargo = spanCargo == null ? null : Textos.vazioComoNulo(spanCargo.ownText());
        Element spanEscolaridade = spanCargo == null ? null : primeiroFilho(spanCargo, "span");
        String escolaridade = spanEscolaridade == null ? null : Textos.vazioComoNulo(spanEscolaridade.text());

        Element celulaPrazo = elemento.selectFirst(".ce");
        String textoPrazo = celulaPrazo == null ? "" : Textos.limpar(celulaPrazo.text());
        DatasTexto.Prazo prazo = DatasTexto.interpretarPrazo(textoPrazo);
        if (prazo.situacao() == DatasTexto.SituacaoPrazo.DESCONHECIDO) {
            avisos.add("Prazo não reconhecido em " + url + ": '" + textoPrazo + "'");
        }

        return new ConcursoListado(url, orgao, titulo, uf, nacional, cargo, escolaridade,
                vagasSalario.vagas(), vagasSalario.cadastroReserva(), vagasSalario.salarioMin(),
                vagasSalario.salarioMax(), prazo.inicio(), prazo.fim(), prazo.situacao());
    }

    private static Element primeiroFilho(Element pai, String tag) {
        for (Element filho : pai.children()) {
            if (filho.normalName().equals(tag)) {
                return filho;
            }
        }
        return null;
    }
}
