package com.pismochallenge.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Pismo Challenge API")
                .version("1.0.0")
                .description("REST API for managing customer accounts and financial transactions. "
                        + "Supports account creation, account querying, and transaction registration "
                        + "with different operation types (purchase, installment purchase, withdrawal, and payment)."));
    }
}
