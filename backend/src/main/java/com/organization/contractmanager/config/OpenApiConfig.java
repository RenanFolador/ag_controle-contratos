package com.organization.contractmanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI contractManagerOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Contract Manager API")
                .description("API do Sistema de Gestão de Contratos")
                .version("v1"));
    }
}
