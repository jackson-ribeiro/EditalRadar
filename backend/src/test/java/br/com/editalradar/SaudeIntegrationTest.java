package br.com.editalradar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "editalradar.coleta.habilitada=false")
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class SaudeIntegrationTest {

    @Autowired
    private TestRestTemplate http;

    @Test
    void healthRespondeUp() {
        ResponseEntity<Map> resposta = http.getForEntity("/actuator/health", Map.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody()).containsEntry("status", "UP");
    }

    @Test
    void naoExpoeOutrosEndpointsDoActuator() {
        assertThat(http.getForEntity("/actuator/env", String.class).getStatusCode()).isNotEqualTo(HttpStatus.OK);
    }
}
