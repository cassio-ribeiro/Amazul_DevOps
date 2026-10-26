package br.com.fiap.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;

public class PerguntaDTO {
    @NotBlank(message = "O enunciado e obrigatorio")
    @Size(max = 500)
    private String enunciado;

    @NotBlank(message = "A resposta e obrigatoria")
    @Pattern(regexp = "[VF]", message = "Use V para verdade ou F para mito")
    @Schema(
            description = "V para verdade; F para mito",
            allowableValues = {"V", "F"},
            example = "V")
    private String resposta;

    @NotBlank(message = "A explicacao e obrigatoria")
    @Size(max = 1000)
    private String explicacao;

    @Size(max = 300)
    private String referencia;

    public PerguntaDTO() {}

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String value) {
        enunciado = value;
    }

    public String getResposta() {
        return resposta;
    }

    public void setResposta(String value) {
        resposta = value;
    }

    public String getExplicacao() {
        return explicacao;
    }

    public void setExplicacao(String value) {
        explicacao = value;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String value) {
        referencia = value;
    }
}
