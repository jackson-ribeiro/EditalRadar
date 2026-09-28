package br.com.editalradar.scraper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BancasTest {

    @Test
    void mapeiaDominiosConhecidos() {
        assertThat(Bancas.deUrl("https://www.quadrix.org.br/login")).contains("Quadrix");
        assertThat(Bancas.deUrl("https://www.institutoconsulplan.org.br/")).contains("Consulplan");
        assertThat(Bancas.deUrl("https://www.ibgpconcursos.com.br/")).contains("IBGP");
        assertThat(Bancas.deUrl("https://conhecimento.fgv.br/concursos/x")).contains("FGV");
    }

    @Test
    void ignoraOrgaosPublicosPciERedesSociais() {
        assertThat(Bancas.deUrl("https://www.crbio01.gov.br/")).isEmpty();
        assertThat(Bancas.deUrl("https://www.unai.mg.leg.br/")).isEmpty();
        assertThat(Bancas.deUrl("https://portal.contagem.mg.gov.br/")).isEmpty();
        assertThat(Bancas.deUrl("https://www.pciconcursos.com.br/apostilas/x")).isEmpty();
        assertThat(Bancas.deUrl("https://www.instagram.com/perfil")).isEmpty();
        assertThat(Bancas.deUrl("#podcast")).isEmpty();
        assertThat(Bancas.deUrl("url com espaço")).isEmpty();
    }

    @Test
    void dominioDesconhecidoViraOProprioHost() {
        assertThat(Bancas.deUrl("https://www.novabanca.org.br/inscricao")).contains("novabanca.org.br");
    }
}
