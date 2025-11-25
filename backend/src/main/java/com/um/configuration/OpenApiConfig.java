package com.um.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Configuration class for Swagger/OpenAPI documentation.
 * <p>
 * Defines API metadata, version, and JWT security scheme
 * for integration with Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates and configures the OpenAPI bean.
     * <p>
     * - Adds API title, version, and description  
     * - Registers JWT Bearer authentication scheme  
     * - Applies security requirement globally across all endpoints
     *
     * @return a customized {@link OpenAPI} instance with JWT authentication support
     */
    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Access management demo Api")
                        .version("1.0")
                        .description("API with JWT authorization"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
