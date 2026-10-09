package com.hampcoders.electrolink.assets.application.internal.commandservices;

import com.hampcoders.electrolink.assets.domain.model.aggregates.Component;
import com.hampcoders.electrolink.assets.domain.model.commands.CreateComponentCommand;
import com.hampcoders.electrolink.assets.domain.model.commands.DeleteComponentCommand;
import com.hampcoders.electrolink.assets.domain.model.commands.UpdateComponentCommand;
import com.hampcoders.electrolink.assets.infrastructure.persistence.jpa.repositories.ComponentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComponentCommandServiceImplTest {

    @Mock
    private ComponentRepository componentRepository;

    @InjectMocks
    private ComponentCommandServiceImpl service;

    private static Component componentWithUid(Long uid, String name, String description) {
        var component = new Component(new CreateComponentCommand(UUID.randomUUID(), name, description, 1L, true));
        ReflectionTestUtils.setField(component, "componentUid", uid);
        return component;
    }

    @Test
    @DisplayName("Create: guarda el componente y devuelve su ComponentId")
    void createComponent_whenNameIsNew_returnsComponentId() {
        // Arrange
        var command = new CreateComponentCommand(UUID.randomUUID(), "Breaker 20A", "Interruptor", 1L, true);
        when(componentRepository.existsByName("Breaker 20A")).thenReturn(false);
        when(componentRepository.save(any(Component.class))).thenReturn(componentWithUid(10L, "Breaker 20A", "Interruptor"));

        // Act
        var result = service.handle(command);

        // Assert
        assertThat(result.componentId()).isEqualTo(10L);
        verify(componentRepository).save(any(Component.class));
    }

    @Test
    @DisplayName("Create: lanza IllegalStateException si el nombre ya existe")
    void createComponent_whenNameExists_throwsIllegalStateException() {
        // Arrange
        var command = new CreateComponentCommand(UUID.randomUUID(), "Breaker 20A", "Interruptor", 1L, true);
        when(componentRepository.existsByName("Breaker 20A")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> service.handle(command)).isInstanceOf(IllegalStateException.class).hasMessage("Component with the same name already exists");
        verify(componentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update: actualiza nombre y descripción del componente existente")
    void updateComponent_whenComponentExists_updatesAndReturnsComponent() {
        // Arrange
        var existing = componentWithUid(5L, "Old", "Old desc");
        var command = new UpdateComponentCommand(5L, "New", "New desc", 1L, true);
        when(componentRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(componentRepository.save(existing)).thenReturn(existing);

        // Act
        var result = service.handle(command);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("New");
        assertThat(result.get().getDescription()).isEqualTo("New desc");
        verify(componentRepository).save(existing);
    }

    @Test
    @DisplayName("Update: devuelve Optional vacío si el componente no existe")
    void updateComponent_whenComponentDoesNotExist_returnsEmpty() {
        // Arrange
        var command = new UpdateComponentCommand(99L, "New", "New desc", 1L, true);
        when(componentRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        var result = service.handle(command);

        // Assert
        assertThat(result).isEmpty();
        verify(componentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Delete: elimina el componente y devuelve true si existe")
    void deleteComponent_whenComponentExists_returnsTrue() {
        // Arrange
        when(componentRepository.existsById(7L)).thenReturn(true);

        // Act
        var result = service.handle(new DeleteComponentCommand(7L));

        // Assert
        assertThat(result).isTrue();
        verify(componentRepository).deleteById(7L);
    }

    @Test
    @DisplayName("Delete: devuelve false y no elimina si el componente no existe")
    void deleteComponent_whenComponentDoesNotExist_returnsFalse() {
        // Arrange
        when(componentRepository.existsById(7L)).thenReturn(false);

        // Act
        var result = service.handle(new DeleteComponentCommand(7L));

        // Assert
        assertThat(result).isFalse();
        verify(componentRepository, never()).deleteById(any());
    }
}
