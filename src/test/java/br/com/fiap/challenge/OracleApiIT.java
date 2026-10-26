package br.com.fiap.challenge;

import static org.junit.jupiter.api.Assertions.*;

import br.com.fiap.challenge.dto.PerguntaDTO;
import br.com.fiap.challenge.repository.PerguntaRepository;
import br.com.fiap.challenge.service.PerguntaService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Executado somente pelo perfil Maven oracle-check. Preserva os registros existentes.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("oracle")
class OracleApiIT {
    @LocalServerPort int port;
    @Autowired PerguntaRepository repository;
    @Autowired PerguntaService service;
    @Autowired PlatformTransactionManager transactionManager;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();
    private final List<Long> registrosCriados = new ArrayList<>();
    private long totalInicial;

    @BeforeEach
    void registrarTotal() {
        totalInicial = repository.count();
    }

    @AfterEach
    void limparSomenteRegistrosDoTeste() throws Exception {
        try {
            for (Long id : registrosCriados) {
                var resposta = request("DELETE", "/perguntas/" + id, null);
                assertTrue(resposta.statusCode() == 204 || resposta.statusCode() == 404);
            }
            assertEquals(totalInicial, repository.count(), "O acervo anterior deve ser preservado");
        } finally {
            client.close();
        }
    }

    private HttpResponse<String> request(String method, String path, String body) throws Exception {
        var builder =
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                        .header("Content-Type", "application/json");
        return client.send(
                builder.method(
                                method,
                                body == null
                                        ? HttpRequest.BodyPublishers.noBody()
                                        : HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private PerguntaDTO exemplo() {
        PerguntaDTO dto = new PerguntaDTO();
        dto.setEnunciado("Teste de integracao Oracle " + UUID.randomUUID());
        dto.setResposta("V");
        dto.setExplicacao("Conteudo exclusivo do teste automatizado, removido apos a verificacao.");
        dto.setReferencia("Verificacao de integracao do projeto Nucleo");
        return dto;
    }

    @Test
    void crudCompletoPorProceduresELeituraJpa() throws Exception {
        PerguntaDTO dto = exemplo();
        var criado = request("POST", "/perguntas", json.writeValueAsString(dto));
        assertEquals(201, criado.statusCode());
        long id = json.readTree(criado.body()).get("id").asLong();
        registrosCriados.add(id);
        assertEquals(totalInicial + 1, repository.count());
        assertEquals(dto.getEnunciado(), repository.findById(id).orElseThrow().getEnunciado());
        dto.setEnunciado(dto.getEnunciado() + " atualizado");
        dto.setResposta("F");
        dto.setReferencia(null);
        var atualizado = request("PUT", "/perguntas/" + id, json.writeValueAsString(dto));
        assertEquals(200, atualizado.statusCode());
        assertEquals(
                dto.getEnunciado(), json.readTree(atualizado.body()).get("enunciado").asText());
        assertTrue(json.readTree(atualizado.body()).get("referencia").isNull());
        assertEquals(
                dto.getEnunciado(),
                json.readTree(request("GET", "/perguntas/" + id, null).body())
                        .get("enunciado")
                        .asText());
        String respostaF = json.createObjectNode().put("resposta", "F").toString();
        var feedback = request("POST", "/jogo/perguntas/" + id + "/respostas", respostaF);
        assertEquals(200, feedback.statusCode());
        assertTrue(json.readTree(feedback.body()).get("acertou").asBoolean());
        var busca = request("GET", "/perguntas?texto=" + id + "inexistente", null);
        assertEquals(200, busca.statusCode());
        assertEquals(204, request("DELETE", "/perguntas/" + id, null).statusCode());
        assertEquals(404, request("GET", "/perguntas/" + id, null).statusCode());
        registrosCriados.remove(id);
    }

    @Test
    void procedureParticipaDoRollbackDaTransacaoJava() {
        Long id =
                new TransactionTemplate(transactionManager)
                        .execute(
                                status -> {
                                    var pergunta = service.cadastrar(exemplo());
                                    status.setRollbackOnly();
                                    return pergunta.getId();
                                });
        assertNotNull(id);
        registrosCriados.add(id);
        assertFalse(repository.existsById(id), "Procedure nao deve executar COMMIT proprio");
    }

    @Test
    void constraintOracleRejeitaGabaritoInvalido() {
        PerguntaDTO dto = exemplo();
        dto.setResposta("X");
        assertThrows(
                DataAccessException.class,
                () -> {
                    var inesperado = service.cadastrar(dto);
                    registrosCriados.add(inesperado.getId());
                });
        assertEquals(totalInicial, repository.count());
    }
}
