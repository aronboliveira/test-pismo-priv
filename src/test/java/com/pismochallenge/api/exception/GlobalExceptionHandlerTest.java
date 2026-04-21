package com.pismochallenge.api.exception;

import com.pismochallenge.api.dto.response.ErrorResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_shouldReturn404WithMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(
                new ResourceNotFoundException("Account not found"));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().message()).isEqualTo("Account not found");
    }

    @Test
    void handleValidation_shouldReturn400_aggregatingFieldErrors() throws Exception {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(new FieldError("object", "documentNumber", "must not be blank"));
        bindingResult.addError(new FieldError("object", "amount", "must be positive"));

        Method method = Sample.class.getDeclaredMethod("dummy");
        MethodParameter parameter = new MethodParameter(method, -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .contains("documentNumber: must not be blank")
                .contains("amount: must be positive");
    }

    @Test
    void handleValidation_shouldFallbackMessage_whenNoFieldErrors() throws Exception {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");

        Method method = Sample.class.getDeclaredMethod("dummy");
        MethodParameter parameter = new MethodParameter(method, -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().message()).isEqualTo("Validation failed");
    }

    @Test
    void handleDataIntegrity_shouldReturn422() {
        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(
                new DataIntegrityViolationException("Duplicate document_number"));

        assertThat(response.getStatusCode().value()).isEqualTo(422);
        assertThat(response.getBody().error()).isEqualTo("Unprocessable Entity");
        assertThat(response.getBody().message()).contains("Data integrity violation");
    }

    @Test
    void handleCircuitBreakerOpen_shouldReturn503() {
        CircuitBreaker cb = CircuitBreaker.of("test", CircuitBreakerConfig.ofDefaults());
        cb.transitionToOpenState();
        CallNotPermittedException ex = CallNotPermittedException.createCallNotPermittedException(cb);

        ResponseEntity<ErrorResponse> response = handler.handleCircuitBreakerOpen(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(503);
        assertThat(response.getBody().message()).contains("Service temporarily unavailable");
    }

    @Test
    void handleDataAccess_shouldReturn503() {
        ResponseEntity<ErrorResponse> response = handler.handleDataAccess(
                new DataAccessException("DB down") {});

        assertThat(response.getStatusCode().value()).isEqualTo(503);
        assertThat(response.getBody().message()).contains("Database temporarily unavailable");
    }

    @Test
    void handleGeneric_shouldReturn500() {
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(new RuntimeException("boom"));

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
    }

    @SuppressWarnings("unused")
    private static final class Sample {
        void dummy() {}
    }
}
