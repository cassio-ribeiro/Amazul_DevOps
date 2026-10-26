package br.com.fiap.challenge.exception;

import java.time.Instant;
import java.util.Map;

public record ErroResponse(
        Instant timestamp, int status, String mensagem, Map<String, String> campos) {}
