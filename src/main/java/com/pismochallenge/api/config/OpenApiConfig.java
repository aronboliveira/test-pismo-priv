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
                .description("API REST para gerenciamento de contas de clientes e transações financeiras. "
                        + "Suporta criação de contas, consulta de contas e registro de transações "
                        + "com diferentes tipos de operação (compra, compra parcelada, saque e pagamento)."));
    }
}
