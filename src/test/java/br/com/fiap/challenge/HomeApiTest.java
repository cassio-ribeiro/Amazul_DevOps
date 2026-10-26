package br.com.fiap.challenge;

import static org.junit.jupiter.api.Assertions.*;

import br.com.fiap.challenge.entity.Pergunta;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.*;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HomeApiTest {
    @LocalServerPort int port;
    @Autowired EntityManager entityManager;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    @AfterEach
    void fechar() {
        client.close();
    }

    @Test
    void raizRetornaIndiceJsonSemWhitelabel() throws Exception {
        var request =
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/")).GET().build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(
                response.headers()
                        .firstValue("Content-Type")
                        .orElseThrow()
                        .contains("application/json"));
        var indice = json.readTree(response.body());
        assertTrue(indice.get("aplicacao").asText().contains("Nucleo"));
        assertEquals("/swagger-ui", indice.get("swagger").asText());
        assertEquals("/v3/api-docs", indice.get("openApi").asText());
        assertEquals(7, indice.get("endpoints").size());
        assertFalse(response.body().contains("Whitelabel"));
        assertFalse(response.body().contains("/usuarios"));
    }

    @Test
    void atalhoSwaggerRedirecionaParaInterface() throws Exception {
        for (String path : new String[] {"/swagger-ui", "/swagger-ui/"}) {
            var request =
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                            .GET()
                            .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(302, response.statusCode());
            assertEquals(
                    "/swagger-ui/index.html",
                    response.headers().firstValue("Location").orElseThrow());
        }
    }

    @Test
    void modeloAtivoContemSomentePergunta() {
        Set<Class<?>> entidades =
                entityManager.getMetamodel().getEntities().stream()
                        .map(e -> (Class<?>) e.getJavaType())
                        .collect(Collectors.toSet());
        assertEquals(Set.of(Pergunta.class), entidades);
    }
}
