package br.com.editalradar.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class EditalRadarPropertiesTest {

    @Test
    void vinculaApplicationYml() throws IOException {
        var fontes = new YamlPropertySourceLoader().load("application", new ClassPathResource("application.yml"));
        var ambiente = new StandardEnvironment();
        fontes.forEach(ambiente.getPropertySources()::addLast);

        EditalRadarProperties propriedades = Binder.get(ambiente)
                .bind("editalradar", EditalRadarProperties.class)
                .get();

        assertThat(propriedades.cors().origensPermitidas()).containsExactly("http://localhost:5173");
        assertThat(propriedades.coleta().cron()).isEqualTo("0 0 7 * * *");
        assertThat(propriedades.coleta().zona()).isEqualTo("America/Sao_Paulo");
        assertThat(propriedades.coleta().delayMin()).isEqualTo(Duration.ofSeconds(1));
        assertThat(propriedades.coleta().delayMax()).isEqualTo(Duration.ofSeconds(2));
        assertThat(propriedades.coleta().timeout()).isEqualTo(Duration.ofSeconds(20));
        assertThat(propriedades.coleta().maxTentativas()).isEqualTo(3);
        assertThat(propriedades.coleta().mcp().termos()).contains("analista de sistemas", "tecnologia da informação");
        assertThat(propriedades.coleta().previstos().maxIdadeDias()).isEqualTo(180);
        assertThat(propriedades.ti().palavrasChave()).contains("tecnologia da informação", "analista de ti");
        assertThat(propriedades.ti().exclusoes()).contains("noções de informática");
        assertThat(propriedades.ti().termosEnsino()).contains("professor", "peb", "bncc");
    }
}
