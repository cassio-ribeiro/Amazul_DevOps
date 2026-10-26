package br.com.fiap.challenge.dto;

public record ResultadoDTO(
        Long perguntaId,
        boolean acertou,
        String respostaCorreta,
        String explicacao,
        String referencia) {}
