package br.com.editalradar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(EditalRadarProperties propriedades) {
        return Clock.system(ZoneId.of(propriedades.coleta().zona()));
    }
}
