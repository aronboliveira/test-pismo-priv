package com.pismochallenge.api.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ResourceNotFoundExceptionTest {

    @Test
    void shouldCarryMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("missing");

        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("missing");
    }
}
