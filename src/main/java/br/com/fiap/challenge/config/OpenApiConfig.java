package br.com.fiap.challenge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Nucleo - Mitos e Verdades Nucleares")
                                .version("0.1.0")
                                .description(
                                        "API educativa FIAP / AMAZUL. V = verdade; F = mito."
                                                + " Prototipo sem autenticacao e sem ranking."));
    }
}
