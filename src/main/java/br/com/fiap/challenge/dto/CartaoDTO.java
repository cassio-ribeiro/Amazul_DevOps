package br.com.fiap.challenge.dto;

// O cartao nao expõe o gabarito nem a explicacao antes da resposta.
public record CartaoDTO(Long id, String enunciado) {}
