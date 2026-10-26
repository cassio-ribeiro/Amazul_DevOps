package br.com.fiap.challenge.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@Tag(name = "Indice da API")
public class HomeController {
    @GetMapping(value = "/", produces = "application/json")
    @Operation(summary = "Apresentar a API e seus endpoints")
    @ApiResponse(responseCode = "200", description = "Indice da API", useReturnTypeSchema = true)
    public Indice home() {
        return new Indice(
                "Nucleo - Mitos e Verdades Nucleares",
                "API Java de perguntas educativas sobre tecnologia nuclear",
                "/swagger-ui",
                "/v3/api-docs",
                List.of(
                        new Endpoint("GET", "/perguntas", "Listar, pesquisar, paginar e ordenar"),
                        new Endpoint("POST", "/perguntas", "Cadastrar pergunta"),
                        new Endpoint("GET", "/perguntas/{id}", "Consultar pergunta"),
                        new Endpoint("PUT", "/perguntas/{id}", "Atualizar pergunta"),
                        new Endpoint("DELETE", "/perguntas/{id}", "Excluir pergunta"),
                        new Endpoint(
                                "GET", "/jogo/perguntas/aleatoria", "Sortear cartao sem gabarito"),
                        new Endpoint(
                                "POST",
                                "/jogo/perguntas/{id}/respostas",
                                "Conferir resposta e obter explicacao")));
    }

    @Hidden
    @GetMapping({"/swagger-ui", "/swagger-ui/"})
    public ResponseEntity<Void> atalhoSwagger() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/swagger-ui/index.html"))
                .build();
    }

    public record Indice(
            String aplicacao,
            String descricao,
            String swagger,
            String openApi,
            List<Endpoint> endpoints) {}

    public record Endpoint(String metodo, String caminho, String descricao) {}
}
