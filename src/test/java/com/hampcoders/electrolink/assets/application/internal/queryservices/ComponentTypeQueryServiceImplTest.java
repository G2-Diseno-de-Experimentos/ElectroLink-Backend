package com.hampcoders.electrolink.assets.application.internal.queryservices;

import com.hampcoders.electrolink.assets.domain.model.aggregates.ComponentType;
import com.hampcoders.electrolink.assets.domain.model.commands.CreateComponentTypeCommand;
import com.hampcoders.electrolink.assets.domain.model.queries.GetAllComponentTypesQuery;
import com.hampcoders.electrolink.assets.domain.model.queries.GetComponentTypeByIdQuery;
import com.hampcoders.electrolink.assets.domain.model.valueobjects.ComponentTypeId;
import com.hampcoders.electrolink.assets.infrastructure.persistence.jpa.repositories.ComponentTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComponentTypeQueryServiceImplTest {

    @Mock
    private ComponentTypeRepository componentTypeRepository;

    @InjectMocks
    private ComponentTypeQueryServiceImpl componentTypeQueryService;

    @Test
    @DisplayName("GetById: devuelve el tipo de componente cuando existe")
    void handleGetComponentTypeByIdQuery_whenComponentTypeExists_returnsComponentType() {
        // Arrange
        var componentTypeId = new ComponentTypeId(1L);
        var expectedComponentType = componentType(1L, "Interruptor");
        when(componentTypeRepository.findById(componentTypeId.id()))
                .thenReturn(Optional.of(expectedComponentType));

        // Act
        var result = componentTypeQueryService.handle(new GetComponentTypeByIdQuery(componentTypeId));

        // Assert
        assertTrue(result.isPresent());
        assertSame(expectedComponentType, result.get());
        verify(componentTypeRepository).findById(componentTypeId.id());
    }

    @Test
    @DisplayName("GetById: devuelve Optional vacío cuando el tipo de componente no existe")
    void handleGetComponentTypeByIdQuery_whenComponentTypeDoesNotExist_returnsEmpty() {
        // Arrange
        var componentTypeId = new ComponentTypeId(99L);
        when(componentTypeRepository.findById(componentTypeId.id())).thenReturn(Optional.empty());

        // Act
        var result = componentTypeQueryService.handle(new GetComponentTypeByIdQuery(componentTypeId));

        // Assert
        assertTrue(result.isEmpty());
        verify(componentTypeRepository).findById(componentTypeId.id());
    }

    @Test
    @DisplayName("GetAll: devuelve todos los tipos de componente")
    void handleGetAllComponentTypesQuery_returnsAllComponentTypes() {
        // Arrange
        var expectedComponentTypes = List.of(
                componentType(1L, "Interruptor"),
                componentType(2L, "Cable")
        );
        when(componentTypeRepository.findAll()).thenReturn(expectedComponentTypes);

        // Act
        var result = componentTypeQueryService.handle(new GetAllComponentTypesQuery());

        // Assert
        assertEquals(expectedComponentTypes, result);
        verify(componentTypeRepository).findAll();
    }

    private static ComponentType componentType(Long id, String name) {
        var componentType = new ComponentType(new CreateComponentTypeCommand(name, "Descripcion"));
        ReflectionTestUtils.setField(componentType, "id", id);
        return componentType;
    }
}
