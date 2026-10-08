package com.hampcoders.electrolink.assets.application.internal.commandservices;

import com.hampcoders.electrolink.assets.domain.model.aggregates.ComponentType;
import com.hampcoders.electrolink.assets.domain.model.commands.CreateComponentTypeCommand;
import com.hampcoders.electrolink.assets.domain.model.commands.DeleteComponentTypeCommand;
import com.hampcoders.electrolink.assets.domain.model.commands.UpdateComponentTypeCommand;
import com.hampcoders.electrolink.assets.infrastructure.persistence.jpa.repositories.ComponentRepository;
import com.hampcoders.electrolink.assets.infrastructure.persistence.jpa.repositories.ComponentTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComponentTypeCommandServiceImplTest {

    @Mock
    private ComponentTypeRepository componentTypeRepository;

    @Mock
    private ComponentRepository componentRepository;

    @InjectMocks
    private ComponentTypeCommandServiceImpl componentTypeCommandService;

    @Test
    @DisplayName("Create: guarda el tipo de componente y devuelve su identificador")
    void handleCreateComponentTypeCommand_whenNameIsAvailable_savesAndReturnsId() {
        // Arrange
        var command = new CreateComponentTypeCommand("Interruptor", "Protege el circuito electrico");
        var savedComponentType = componentType(1L, command.name(), command.description());
        when(componentTypeRepository.existsByName(command.name())).thenReturn(false);
        when(componentTypeRepository.save(any(ComponentType.class))).thenReturn(savedComponentType);

        // Act
        var result = componentTypeCommandService.handle(command);

        // Assert
        assertEquals(1L, result.id());
        verify(componentTypeRepository).existsByName(command.name());
        verify(componentTypeRepository).save(any(ComponentType.class));
    }

    @Test
    @DisplayName("Create: lanza excepción cuando el nombre ya existe")
    void handleCreateComponentTypeCommand_whenNameAlreadyExists_throwsException() {
        // Arrange
        var command = new CreateComponentTypeCommand("Interruptor", "Protege el circuito electrico");
        when(componentTypeRepository.existsByName(command.name())).thenReturn(true);

        // Act
        var exception = assertThrows(
                IllegalStateException.class,
                () -> componentTypeCommandService.handle(command)
        );

        // Assert
        assertEquals("Component type with the same name already exists", exception.getMessage());
        verify(componentTypeRepository, never()).save(any(ComponentType.class));
    }

    @Test
    @DisplayName("Update: actualiza y guarda el tipo de componente cuando existe")
    void handleUpdateComponentTypeCommand_whenComponentTypeExists_updatesAndReturnsIt() {
        // Arrange
        var componentType = componentType(1L, "Interruptor", "Descripcion original");
        var command = new UpdateComponentTypeCommand(1L, "Interruptor termomagnetico", "Descripcion actualizada");
        when(componentTypeRepository.findById(command.id())).thenReturn(Optional.of(componentType));
        when(componentTypeRepository.save(componentType)).thenReturn(componentType);

        // Act
        var result = componentTypeCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertSame(componentType, result.get());
        assertEquals(command.name(), componentType.getName());
        verify(componentTypeRepository).save(componentType);
    }

    @Test
    @DisplayName("Update: devuelve Optional vacío cuando el tipo de componente no existe")
    void handleUpdateComponentTypeCommand_whenComponentTypeDoesNotExist_returnsEmpty() {
        // Arrange
        var command = new UpdateComponentTypeCommand(99L, "Interruptor", "Descripcion");
        when(componentTypeRepository.findById(command.id())).thenReturn(Optional.empty());

        // Act
        var result = componentTypeCommandService.handle(command);

        // Assert
        assertTrue(result.isEmpty());
        verify(componentTypeRepository, never()).save(any(ComponentType.class));
    }

    @Test
    @DisplayName("Delete: elimina y devuelve true cuando el tipo existe y no esta en uso")
    void handleDeleteComponentTypeCommand_whenNotInUse_deletesAndReturnsTrue() {
        // Arrange
        var componentTypeId = 1L;
        when(componentTypeRepository.existsById(componentTypeId)).thenReturn(true);
        when(componentRepository.existsByComponentTypeId(componentTypeId)).thenReturn(false);

        // Act
        var result = componentTypeCommandService.handle(new DeleteComponentTypeCommand(componentTypeId));

        // Assert
        assertTrue(result);
        verify(componentTypeRepository).deleteById(componentTypeId);
    }

    @Test
    @DisplayName("Delete: devuelve false cuando el tipo de componente no existe")
    void handleDeleteComponentTypeCommand_whenComponentTypeDoesNotExist_returnsFalse() {
        // Arrange
        var componentTypeId = 99L;
        when(componentTypeRepository.existsById(componentTypeId)).thenReturn(false);

        // Act
        var result = componentTypeCommandService.handle(new DeleteComponentTypeCommand(componentTypeId));

        // Assert
        assertFalse(result);
        verify(componentRepository, never()).existsByComponentTypeId(any(Long.class));
        verify(componentTypeRepository, never()).deleteById(any(Long.class));
    }

    @Test
    @DisplayName("Delete: lanza excepción cuando el tipo de componente esta en uso")
    void handleDeleteComponentTypeCommand_whenComponentTypeIsInUse_throwsException() {
        // Arrange
        var componentTypeId = 1L;
        when(componentTypeRepository.existsById(componentTypeId)).thenReturn(true);
        when(componentRepository.existsByComponentTypeId(componentTypeId)).thenReturn(true);

        // Act
        var exception = assertThrows(
                IllegalStateException.class,
                () -> componentTypeCommandService.handle(new DeleteComponentTypeCommand(componentTypeId))
        );

        // Assert
        assertEquals(
                "Cannot delete component type: it is currently in use by one or more components.",
                exception.getMessage()
        );
        verify(componentTypeRepository, never()).deleteById(componentTypeId);
    }

    private static ComponentType componentType(Long id, String name, String description) {
        var componentType = new ComponentType(new CreateComponentTypeCommand(name, description));
        ReflectionTestUtils.setField(componentType, "id", id);
        return componentType;
    }
}
