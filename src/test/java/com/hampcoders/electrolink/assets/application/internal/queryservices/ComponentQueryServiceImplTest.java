package com.hampcoders.electrolink.assets.application.internal.queryservices;

import com.hampcoders.electrolink.assets.domain.model.aggregates.Component;
import com.hampcoders.electrolink.assets.domain.model.commands.CreateComponentCommand;
import com.hampcoders.electrolink.assets.domain.model.queries.GetAllComponentsQuery;
import com.hampcoders.electrolink.assets.domain.model.queries.GetComponentByIdQuery;
import com.hampcoders.electrolink.assets.domain.model.queries.GetComponentsByIdsQuery;
import com.hampcoders.electrolink.assets.domain.model.queries.GetComponentsByNameQuery;
import com.hampcoders.electrolink.assets.domain.model.queries.GetComponentsByTypeIdQuery;
import com.hampcoders.electrolink.assets.domain.model.valueobjects.ComponentId;
import com.hampcoders.electrolink.assets.infrastructure.persistence.jpa.repositories.ComponentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComponentQueryServiceImplTest {

    @Mock
    private ComponentRepository componentRepository;

    @InjectMocks
    private ComponentQueryServiceImpl service;

    private static Component component(String name) {
        return new Component(new CreateComponentCommand(UUID.randomUUID(), name, "desc", 1L, true));
    }

    @Test
    @DisplayName("GetById: devuelve el componente cuando existe")
    void getComponentById_whenExists_returnsComponent() {
        // Arrange
        var expected = component("Breaker");
        when(componentRepository.findById(3L)).thenReturn(Optional.of(expected));

        // Act
        var result = service.handle(new GetComponentByIdQuery(new ComponentId(3L)));

        // Assert
        assertThat(result).containsSame(expected);
    }

    @Test
    @DisplayName("GetById: devuelve vacío cuando no existe")
    void getComponentById_whenMissing_returnsEmpty() {
        // Arrange
        when(componentRepository.findById(3L)).thenReturn(Optional.empty());

        // Act
        var result = service.handle(new GetComponentByIdQuery(new ComponentId(3L)));

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("GetAll: devuelve todos los componentes del repositorio")
    void getAllComponents_returnsAllComponents() {
        // Arrange
        var expected = List.of(component("A"), component("B"));
        when(componentRepository.findAll()).thenReturn(expected);

        // Act
        var result = service.handle(new GetAllComponentsQuery());

        // Assert
        assertThat(result).containsExactlyElementsOf(expected);
    }

    @Test
    @DisplayName("GetByTypeId: filtra por el tipo de componente")
    void getComponentsByTypeId_returnsComponentsOfThatType() {
        // Arrange
        var expected = List.of(component("A"));
        when(componentRepository.findByComponentTypeId(2L)).thenReturn(expected);

        // Act
        var result = service.handle(new GetComponentsByTypeIdQuery(2L));

        // Assert
        assertThat(result).containsExactlyElementsOf(expected);
    }

    @Test
    @DisplayName("GetByIds: convierte los ComponentId a Long antes de consultar")
    void getComponentsByIds_mapsIdsAndReturnsComponents() {
        // Arrange
        var expected = List.of(component("A"), component("B"));
        when(componentRepository.findByComponentUidIn(List.of(1L, 2L))).thenReturn(expected);

        // Act
        var result = service.handle(new GetComponentsByIdsQuery(List.of(new ComponentId(1L), new ComponentId(2L))));

        // Assert
        assertThat(result).containsExactlyElementsOf(expected);
        verify(componentRepository).findByComponentUidIn(List.of(1L, 2L));
    }

    @Test
    @DisplayName("GetByName: respeta el límite pedido sobre los resultados del repositorio")
    void getComponentsByName_appliesLimit() {
        // Arrange
        var found = List.of(component("Cable 1"), component("Cable 2"), component("Cable 3"));
        when(componentRepository.findTop10ByNameContainingIgnoreCase("cable")).thenReturn(found);

        // Act
        var result = service.handle(new GetComponentsByNameQuery("cable", 2));

        // Assert
        assertThat(result).hasSize(2).containsExactly(found.get(0), found.get(1));
    }

    @Test
    @DisplayName("GetByName: devuelve lista vacía si no hay coincidencias")
    void getComponentsByName_whenNoMatches_returnsEmptyList() {
        // Arrange
        when(componentRepository.findTop10ByNameContainingIgnoreCase("xyz")).thenReturn(List.of());

        // Act
        var result = service.handle(new GetComponentsByNameQuery("xyz", 5));

        // Assert
        assertThat(result).isEmpty();
    }
}
