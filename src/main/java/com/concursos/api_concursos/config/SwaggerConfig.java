package com.concursos.api_concursos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Monitoramento de Concursos Públicos")
                        .version("1.0.0")
                        .description("Sistema de scraping automatizado e gerenciamento de editais de concursos públicos usando Jsoup e Spring Boot."));
    }
}