package br.com.editalradar.ti;

import br.com.editalradar.config.EditalRadarProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class FiltroTiConfiguradoTest {

    private static FiltroTi filtro;

    @BeforeAll
    static void carregarApplicationYml() throws IOException {
        var fontes = new YamlPropertySourceLoader().load("application", new ClassPathResource("application.yml"));
        var ambiente = new StandardEnvironment();
        fontes.forEach(ambiente.getPropertySources()::addLast);
        filtro = new FiltroTi(Binder.get(ambiente).bind("editalradar", EditalRadarProperties.class).get());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Técnico de TI",
            "Técnico em TI (2 vagas)",
            "Assistente de TI",
            "Gerente de TI",
            "Analista de TI",
            "Técnico em Informática",
            "Analista de Tecnologia da Informação",
            "Agente de Informática",
            "Técnico em Informática (1 vaga)",
            "Monitor de Laboratório de Informática"
    })
    void reconheceCargosComunsDeTi(String cargo) {
        assertThat(filtro.ehTi(cargo)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Médico",
            "Professor de Informática",
            "Prova com noções de informática",
            "Técnico em Enfermagem",
            "Agente de Trânsito",
            "Professor - Informática",
            "Professor de Educação Básica II - Informática (CR)",
            "Projeto BNCC da Computação - Habilitado",
            "PEB II - Informática (1 vaga + CR)",
            "Professor de Computação",
            "Professor II (Informática) (cadastro de reserva)",
            "Professor MAPB - Informática - EMEIEF Vereador Leandro Zinger (cadastro de reserva)",
            "Professor II - Computação (2 vagas)",
            "Professor de Ensino Fundamental - Laboratório de Informática e Telecentro",
            "Instrutor de Informática",
            "Tutor de Tecnologia da Informação EAD"
    })
    void ignoraCargosForaDeTi(String cargo) {
        assertThat(filtro.ehTi(cargo)).isFalse();
    }
}
