package com.pismochallenge.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class OpenApiConfigTest {

    @Test
    void customOpenAPI_shouldExposeProjectMetadata() {
        OpenAPI openApi = new OpenApiConfig().customOpenAPI();

        assertThat(openApi).isNotNull();
        assertThat(openApi.getInfo()).isNotNull();
        assertThat(openApi.getInfo().getTitle()).isEqualTo("Pismo Challenge API");
        assertThat(openApi.getInfo().getVersion()).isEqualTo("1.0.0");
        assertThat(openApi.getInfo().getDescription())
                .contains("REST API")
                .contains("accounts")
                .contains("transactions");
    }
}
