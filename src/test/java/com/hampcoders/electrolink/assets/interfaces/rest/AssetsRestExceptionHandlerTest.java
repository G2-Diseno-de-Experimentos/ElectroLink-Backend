package com.hampcoders.electrolink.assets.interfaces.rest;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class AssetsRestExceptionHandlerTest {

    private final AssetsRestExceptionHandler handler = new AssetsRestExceptionHandler();

    @Test
    @DisplayName("IllegalStateException: devuelve 409 Conflict con el mensaje de la excepción")
    void handleIllegalStateException_whenThrown_returnsConflictWithMessage() {
        // Arrange
        var ex = new IllegalStateException("Component with name already exists");

        // Act
        var response = handler.handleIllegalStateException(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("Component with name already exists");
    }

    @Test
    @DisplayName("EntityNotFoundException: devuelve 404 Not Found con el mensaje de la excepción")
    void handleEntityNotFoundException_whenThrown_returnsNotFoundWithMessage() {
        // Arrange
        var ex = new EntityNotFoundException("Technician inventory not found");

        // Act
        var response = handler.handleEntityNotFoundException(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("Technician inventory not found");
    }

    @Test
    @DisplayName("IllegalArgumentException: devuelve 400 Bad Request con el mensaje de la excepción")
    void handleIllegalArgumentException_whenThrown_returnsBadRequestWithMessage() {
        // Arrange
        var ex = new IllegalArgumentException("Component id must be greater than zero");

        // Act
        var response = handler.handleIllegalArgumentException(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("Component id must be greater than zero");
    }
}
