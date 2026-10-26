package br.com.fiap.challenge.controller;

import br.com.fiap.challenge.dto.PerguntaDTO;
import br.com.fiap.challenge.entity.Pergunta;
import br.com.fiap.challenge.exception.ErroResponse;
import br.com.fiap.challenge.service.PerguntaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(value = "/perguntas", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(
        name = "Administracao de perguntas",
        description = "CRUD destinado ao administrador; autenticacao ainda nao implementada")
@ApiResponse(
        responseCode = "400",
        description = "Requisicao ou campos invalidos",
        content = @Content(schema = @Schema(implementation = ErroResponse.class)))
public class PerguntaController {
    private final PerguntaService service;

    public PerguntaController(PerguntaService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastrar uma pergunta")
    @ApiResponse(
            responseCode = "201",
            description = "Pergunta cadastrada",
            useReturnTypeSchema = true)
    public ResponseEntity<Pergunta> cadastrar(@Valid @RequestBody PerguntaDTO dto) {
        Pergunta p = service.cadastrar(dto);
        return ResponseEntity.created(URI.create("/perguntas/" + p.getId())).body(p);
    }

    @GetMapping
    @Operation(summary = "Listar perguntas com paginacao, ordenacao e busca por enunciado")
    @ApiResponse(
            responseCode = "200",
            description = "Operacao concluida",
            useReturnTypeSchema = true)
    public Page<Pergunta> listar(
            @Parameter(description = "Trecho do enunciado, sem diferenciar maiusculas")
                    @RequestParam(required = false)
                    String texto,
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return service.listar(texto, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar uma pergunta pelo ID")
    @ApiResponse(
            responseCode = "200",
            description = "Operacao concluida",
            useReturnTypeSchema = true)
    @ApiResponse(
            responseCode = "404",
            description = "Pergunta inexistente",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public Pergunta buscar(
            @Parameter(description = "Identificador da pergunta", example = "1") @PathVariable
                    Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar todos os campos de uma pergunta")
    @ApiResponse(
            responseCode = "200",
            description = "Operacao concluida",
            useReturnTypeSchema = true)
    @ApiResponse(
            responseCode = "404",
            description = "Pergunta inexistente",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public Pergunta atualizar(
            @Parameter(description = "Identificador da pergunta", example = "1") @PathVariable
                    Long id,
            @Valid @RequestBody PerguntaDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir uma pergunta")
    @ApiResponse(responseCode = "204", description = "Pergunta excluida", content = @Content)
    @ApiResponse(
            responseCode = "404",
            description = "Pergunta inexistente",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador da pergunta", example = "1") @PathVariable
                    Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
