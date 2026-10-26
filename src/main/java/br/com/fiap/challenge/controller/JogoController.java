package br.com.fiap.challenge.controller;

import br.com.fiap.challenge.dto.*;
import br.com.fiap.challenge.exception.ErroResponse;
import br.com.fiap.challenge.service.PerguntaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/jogo/perguntas", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Mito ou Verdade")
public class JogoController {
    private final PerguntaService service;

    public JogoController(PerguntaService service) {
        this.service = service;
    }

    @GetMapping("/aleatoria")
    @Operation(
            summary = "Sortear um cartao sem gabarito",
            description = "Sorteios independentes podem repetir perguntas")
    @ApiResponse(
            responseCode = "200",
            description = "Cartao sorteado sem gabarito",
            useReturnTypeSchema = true)
    @ApiResponse(
            responseCode = "404",
            description = "Acervo vazio",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public CartaoDTO sortear() {
        return service.sortear();
    }

    @PostMapping("/{id}/respostas")
    @Operation(
            summary = "Conferir resposta e obter explicacao e referencia",
            description = "Nao grava historico nem concede pontos nesta versao")
    @ApiResponse(
            responseCode = "200",
            description = "Resultado com explicacao e referencia",
            useReturnTypeSchema = true)
    @ApiResponse(
            responseCode = "400",
            description = "Resposta diferente de V ou F, ou JSON invalido",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Pergunta inexistente",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResultadoDTO responder(
            @Parameter(description = "Identificador da pergunta sorteada", example = "1")
                    @PathVariable
                    Long id,
            @Valid @RequestBody RespostaDTO dto) {
        return service.responder(id, dto);
    }
}
