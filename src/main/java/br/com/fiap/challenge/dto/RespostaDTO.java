package br.com.fiap.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RespostaDTO {
    @NotBlank
    @Pattern(regexp = "[VF]", message = "Use V para verdade ou F para mito")
    @Schema(
            description = "V para verdade; F para mito",
            allowableValues = {"V", "F"},
            example = "V")
    private String resposta;

    public RespostaDTO() {}

    public String getResposta() {
        return resposta;
    }

    public void setResposta(String value) {
        resposta = value;
    }
}
