package br.com.editalradar.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final EditalRadarProperties propriedades;

    public WebConfig(EditalRadarProperties propriedades) {
        this.propriedades = propriedades;
    }

    @Override
    public void addCorsMappings(CorsRegistry registro) {
        registro.addMapping("/api/**")
                .allowedOrigins(propriedades.cors().origensPermitidas().toArray(String[]::new))
                .allowedMethods("GET", "POST");
    }
}
