package br.com.editalradar.scraper;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;

public final class Bancas {

    private static final Map<String, String> CONHECIDAS = Map.ofEntries(
            entry("quadrix.org.br", "Quadrix"),
            entry("institutoconsulplan.org.br", "Consulplan"),
            entry("ibgpconcursos.com.br", "IBGP"),
            entry("cebraspe.org.br", "Cebraspe"),
            entry("fgv.br", "FGV"),
            entry("vunesp.com.br", "Vunesp"),
            entry("ibfc.org.br", "IBFC"),
            entry("institutoaocp.org.br", "Instituto AOCP"),
            entry("concursosfcc.com.br", "FCC"),
            entry("fcc.org.br", "FCC"),
            entry("cesgranrio.org.br", "Cesgranrio"),
            entry("idecan.org.br", "Idecan"),
            entry("ibade.org.br", "IBADE"),
            entry("fundatec.org.br", "Fundatec"),
            entry("objetivas.com.br", "Objetiva"),
            entry("legalleconcursos.com.br", "Legalle"),
            entry("iades.com.br", "IADES"),
            entry("selecon.org.br", "Selecon"),
            entry("fepese.org.br", "Fepese"),
            entry("fumarc.com.br", "Fumarc"),
            entry("ibam-concursos.org.br", "IBAM"),
            entry("avancasp.org.br", "Avança SP"),
            entry("institutomais.org.br", "Instituto Mais"),
            entry("rbo.org.br", "RBO"),
            entry("consulpam.com.br", "Consulpam"),
            entry("cetrede.com.br", "Cetrede"));

    private static final List<String> SUFIXOS_IGNORADOS = List.of(".gov.br", ".leg.br", ".jus.br", ".mp.br", ".def.br");

    private static final List<String> DOMINIOS_IGNORADOS = List.of(
            "pciconcursos.com.br", "pci.app.br", "facebook.com", "instagram.com", "youtube.com", "youtu.be",
            "twitter.com", "x.com", "wa.me", "whatsapp.com", "spotify.com", "linkedin.com", "t.me");

    private Bancas() {
    }

    public static Optional<String> deUrl(String url) {
        String host;
        try {
            host = URI.create(url).getHost();
        } catch (IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
        if (host == null) {
            return Optional.empty();
        }
        host = host.toLowerCase(Locale.ROOT);
        if (host.startsWith("www.")) {
            host = host.substring(4);
        }
        if (ignorado(host)) {
            return Optional.empty();
        }
        for (Map.Entry<String, String> banca : CONHECIDAS.entrySet()) {
            if (pertence(host, banca.getKey())) {
                return Optional.of(banca.getValue());
            }
        }
        return Optional.of(host);
    }

    private static boolean ignorado(String host) {
        return SUFIXOS_IGNORADOS.stream().anyMatch(host::endsWith)
                || DOMINIOS_IGNORADOS.stream().anyMatch(dominio -> pertence(host, dominio));
    }

    private static boolean pertence(String host, String dominio) {
        return host.equals(dominio) || host.endsWith("." + dominio);
    }
}
