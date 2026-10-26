package br.com.fiap.challenge;

import static org.junit.jupiter.api.Assertions.*;

import br.com.fiap.challenge.repository.PerguntaRepository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ChallengeApiTest {
    @LocalServerPort int port;
    @Autowired PerguntaRepository repository;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();
    private static final String PERGUNTA =
            """
            {"enunciado":"A tecnologia nuclear tem aplicacoes na medicina?", "resposta":"V", "explicacao":"Sim, no diagnostico e no tratamento.", "referencia":"Material didatico de teste"}
            """;

    @BeforeEach
    void limpar() {
        repository.deleteAll();
    }

    HttpResponse<String> request(String method, String path, String body) throws Exception {
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

    @Test
    void crudBuscaPaginacaoESorteioComLacunaNosIds() throws Exception {
        var primeira = request("POST", "/perguntas", PERGUNTA);
        assertEquals(201, primeira.statusCode());
        long id1 = json.readTree(primeira.body()).get("id").asLong();
        var segunda = request("POST", "/perguntas", PERGUNTA.replace("medicina", "agricultura"));
        long id2 = json.readTree(segunda.body()).get("id").asLong();
        assertEquals("/perguntas/" + id1, primeira.headers().firstValue("Location").orElseThrow());
        JsonNode busca =
                json.readTree(
                        request("GET", "/perguntas?texto=MEDICINA&size=1&sort=id,desc", null)
                                .body());
        assertEquals(1, busca.get("totalElements").asInt());
        assertEquals(id1, busca.get("content").get(0).get("id").asLong());
        assertEquals(
                200,
                request("PUT", "/perguntas/" + id2, PERGUNTA.replace("medicina", "industria"))
                        .statusCode());
        assertTrue(request("GET", "/perguntas/" + id2, null).body().contains("industria"));
        assertEquals(204, request("DELETE", "/perguntas/" + id1, null).statusCode());
        assertEquals(404, request("GET", "/perguntas/" + id1, null).statusCode());
        var sorteio = request("GET", "/jogo/perguntas/aleatoria", null);
        assertEquals(200, sorteio.statusCode());
        JsonNode cartao = json.readTree(sorteio.body());
        assertEquals(id2, cartao.get("id").asLong());
        assertEquals(2, cartao.size());
        assertFalse(cartao.has("resposta"));
        assertFalse(cartao.has("explicacao"));
    }

    @Test
    void feedbackCorretoEIncorreto() throws Exception {
        long id = json.readTree(request("POST", "/perguntas", PERGUNTA).body()).get("id").asLong();
        JsonNode certo =
                json.readTree(
                        request(
                                        "POST",
                                        "/jogo/perguntas/" + id + "/respostas",
                                        "{\"resposta\":\"V\"}")
                                .body());
        assertTrue(certo.get("acertou").asBoolean());
        assertEquals("V", certo.get("respostaCorreta").asText());
        assertFalse(certo.get("explicacao").asText().isBlank());
        JsonNode errado =
                json.readTree(
                        request(
                                        "POST",
                                        "/jogo/perguntas/" + id + "/respostas",
                                        "{\"resposta\":\"F\"}")
                                .body());
        assertFalse(errado.get("acertou").asBoolean());
    }

    @Test
    void rejeitaDadosInvalidosSemGravar() throws Exception {
        assertEquals(400, request("POST", "/perguntas", "{}").statusCode());
        assertEquals(
                400,
                request("POST", "/perguntas", PERGUNTA.replace("\"V\"", "\"X\"")).statusCode());
        assertEquals(
                400,
                request(
                                "POST",
                                "/perguntas",
                                PERGUNTA.replace(
                                        "A tecnologia nuclear tem aplicacoes na medicina?",
                                        "x".repeat(501)))
                        .statusCode());
        assertEquals(400, request("POST", "/perguntas", "{malformado").statusCode());
        assertEquals(400, request("GET", "/perguntas/abc", null).statusCode());
        assertEquals(400, request("GET", "/perguntas?sort=inexistente", null).statusCode());
        assertEquals(0, repository.count());
    }

    @Test
    void acervoVazioEAtualizacaoInexistente() throws Exception {
        assertEquals(404, request("GET", "/jogo/perguntas/aleatoria", null).statusCode());
        assertEquals(404, request("PUT", "/perguntas/999999", PERGUNTA).statusCode());
        assertEquals(404, request("DELETE", "/perguntas/999999", null).statusCode());
        assertEquals(0, repository.count());
    }

    @Test
    void swaggerDocumentaOsContratos() throws Exception {
        var response = request("GET", "/v3/api-docs", null);
        assertEquals(200, response.statusCode());
        JsonNode paths = json.readTree(response.body()).get("paths");
        assertTrue(paths.has("/perguntas"));
        assertTrue(paths.has("/jogo/perguntas/aleatoria"));
        assertTrue(paths.has("/jogo/perguntas/{id}/respostas"));
        assertEquals(200, request("GET", "/swagger-ui/index.html", null).statusCode());
    }

    @Test
    void validacaoDeAtualizacaoPreservaRegistro() throws Exception {
        long id = json.readTree(request("POST", "/perguntas", PERGUNTA).body()).get("id").asLong();
        var payload = json.readTree(PERGUNTA).deepCopy();
        ((tools.jackson.databind.node.ObjectNode) payload).put("explicacao", "   ");
        var response = request("PUT", "/perguntas/" + id, payload.toString());
        assertEquals(400, response.statusCode());
        JsonNode erro = json.readTree(response.body());
        assertEquals(400, erro.get("status").asInt());
        assertTrue(erro.get("campos").has("explicacao"));
        assertEquals(
                "Sim, no diagnostico e no tratamento.",
                json.readTree(request("GET", "/perguntas/" + id, null).body())
                        .get("explicacao")
                        .asText());
        assertEquals(1, repository.count());
    }

    @Test
    void limitesDosCamposEReferenciaOpcional() throws Exception {
        var payload =
                json.createObjectNode()
                        .put("enunciado", "a".repeat(500))
                        .put("resposta", "F")
                        .put("explicacao", "b".repeat(1000))
                        .put("referencia", "c".repeat(300));
        assertEquals(201, request("POST", "/perguntas", payload.toString()).statusCode());
        payload.put("explicacao", "b".repeat(1001));
        assertEquals(400, request("POST", "/perguntas", payload.toString()).statusCode());
        payload.put("explicacao", "Explicacao valida").put("referencia", "c".repeat(301));
        assertEquals(400, request("POST", "/perguntas", payload.toString()).statusCode());
        payload.remove("referencia");
        JsonNode criado = json.readTree(request("POST", "/perguntas", payload.toString()).body());
        assertTrue(criado.get("referencia").isNull());
        payload.put("referencia", "  ");
        assertTrue(
                json.readTree(request("POST", "/perguntas", payload.toString()).body())
                        .get("referencia")
                        .isNull());
        assertEquals(3, repository.count());
    }

    @Test
    void paginacaoOrdenacaoELimiteDePagina() throws Exception {
        long a = json.readTree(request("POST", "/perguntas", PERGUNTA).body()).get("id").asLong();
        long b = json.readTree(request("POST", "/perguntas", PERGUNTA).body()).get("id").asLong();
        JsonNode primeira =
                json.readTree(request("GET", "/perguntas?page=0&size=1&sort=id,desc", null).body());
        JsonNode segunda =
                json.readTree(request("GET", "/perguntas?page=1&size=1&sort=id,desc", null).body());
        assertEquals(b, primeira.get("content").get(0).get("id").asLong());
        assertEquals(a, segunda.get("content").get(0).get("id").asLong());
        assertEquals(2, primeira.get("totalPages").asInt());
        assertEquals(
                100,
                json.readTree(request("GET", "/perguntas?size=9999", null).body())
                        .get("size")
                        .asInt());
        assertEquals(
                0,
                json.readTree(request("GET", "/perguntas?texto=inexistente", null).body())
                        .get("totalElements")
                        .asInt());
    }

    @Test
    void respostasInvalidasENaoEncontradas() throws Exception {
        long id = json.readTree(request("POST", "/perguntas", PERGUNTA).body()).get("id").asLong();
        for (String resposta : new String[] {"", "v", "verdade", "X"}) {
            String body = json.createObjectNode().put("resposta", resposta).toString();
            assertEquals(
                    400,
                    request("POST", "/jogo/perguntas/" + id + "/respostas", body).statusCode());
        }
        assertEquals(
                400, request("POST", "/jogo/perguntas/" + id + "/respostas", "{}").statusCode());
        String verdade = json.createObjectNode().put("resposta", "V").toString();
        assertEquals(
                404, request("POST", "/jogo/perguntas/999999/respostas", verdade).statusCode());
        var falso = (tools.jackson.databind.node.ObjectNode) json.readTree(PERGUNTA);
        falso.put("resposta", "F");
        long mito =
                json.readTree(request("POST", "/perguntas", falso.toString()).body())
                        .get("id")
                        .asLong();
        String respostaF = json.createObjectNode().put("resposta", "F").toString();
        assertTrue(
                json.readTree(
                                request("POST", "/jogo/perguntas/" + mito + "/respostas", respostaF)
                                        .body())
                        .get("acertou")
                        .asBoolean());
    }

    @Test
    void buscaTrataCuringasComoTextoLiteral() throws Exception {
        request("POST", "/perguntas", PERGUNTA);
        request("POST", "/perguntas", PERGUNTA.replace("medicina", "100%_medicina"));
        JsonNode found = json.readTree(request("GET", "/perguntas?texto=%25_", null).body());
        assertEquals(1, found.get("totalElements").asInt());
    }

    @Test
    void openApiDocumentaSucessoErrosSchemasEParametros() throws Exception {
        JsonNode api = json.readTree(request("GET", "/v3/api-docs", null).body());
        String[][] operacoes = {
            {"/perguntas", "post", "201"},
            {"/perguntas", "get", "200"},
            {"/perguntas/{id}", "get", "200"},
            {"/perguntas/{id}", "put", "200"},
            {"/jogo/perguntas/aleatoria", "get", "200"},
            {"/jogo/perguntas/{id}/respostas", "post", "200"}
        };
        for (String[] op : operacoes) {
            JsonNode response = api.get("paths").get(op[0]).get(op[1]).get("responses").get(op[2]);
            assertNotNull(response, String.join(" ", op));
            assertNotNull(response.get("content").get("application/json").get("schema"));
        }
        JsonNode delete =
                api.get("paths").get("/perguntas/{id}").get("delete").get("responses").get("204");
        assertNotNull(delete);
        assertTrue(!delete.has("content") || delete.get("content").isEmpty());
        JsonNode erro =
                api.get("paths").get("/perguntas/{id}").get("get").get("responses").get("404");
        assertEquals(
                "#/components/schemas/ErroResponse",
                erro.get("content").get("application/json").get("schema").get("$ref").asText());
        JsonNode parametros = api.get("paths").get("/perguntas").get("get").get("parameters");
        for (String nome : new String[] {"texto", "page", "size", "sort"}) {
            boolean found = false;
            for (JsonNode parametro : parametros)
                if (nome.equals(parametro.get("name").asText())) found = true;
            assertTrue(found, "Parametro documentado: " + nome);
        }
    }
}
