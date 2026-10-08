package com.hampcoders.electrolink.assets.application.internal.queryservices;

import com.hampcoders.electrolink.assets.domain.model.aggregates.Property;
import com.hampcoders.electrolink.assets.domain.model.commands.CreatePropertyCommand;
import com.hampcoders.electrolink.assets.domain.model.queries.GetAllPropertiesByOwnerIdQuery;
import com.hampcoders.electrolink.assets.domain.model.queries.GetAllPropertiesQuery;
import com.hampcoders.electrolink.assets.domain.model.queries.GetPropertyByIdQuery;
import com.hampcoders.electrolink.assets.domain.model.valueobjects.Address;
import com.hampcoders.electrolink.assets.domain.model.valueobjects.District;
import com.hampcoders.electrolink.assets.domain.model.valueobjects.OwnerId;
import com.hampcoders.electrolink.assets.domain.model.valueobjects.Region;
import com.hampcoders.electrolink.assets.infrastructure.persistence.jpa.repositories.PropertyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyQueryServiceImplTest {

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private PropertyQueryServiceImpl propertyQueryService;

    @Test
    @DisplayName("GetById: devuelve la propiedad cuando existe")
    void handleGetPropertyByIdQuery_whenPropertyExists_returnsProperty() {
        // Arrange
        var propertyId = UUID.randomUUID();
        var expectedProperty = property(new OwnerId(10L));
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(expectedProperty));

        // Act
        var result = propertyQueryService.handle(new GetPropertyByIdQuery(propertyId));

        // Assert
        assertTrue(result.isPresent());
        assertSame(expectedProperty, result.get());
        verify(propertyRepository).findById(propertyId);
    }

    @Test
    @DisplayName("GetById: devuelve Optional vacío cuando la propiedad no existe")
    void handleGetPropertyByIdQuery_whenPropertyDoesNotExist_returnsEmpty() {
        // Arrange
        var propertyId = UUID.randomUUID();
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        // Act
        var result = propertyQueryService.handle(new GetPropertyByIdQuery(propertyId));

        // Assert
        assertTrue(result.isEmpty());
        verify(propertyRepository).findById(propertyId);
    }

    @Test
    @DisplayName("GetByOwnerId: devuelve las propiedades del propietario")
    void handleGetAllPropertiesByOwnerIdQuery_returnsOwnerProperties() {
        // Arrange
        var ownerId = new OwnerId(10L);
        var expectedProperties = List.of(property(ownerId), property(ownerId));
        when(propertyRepository.findPropertiesByOwnerId(ownerId)).thenReturn(expectedProperties);

        // Act
        var result = propertyQueryService.handle(new GetAllPropertiesByOwnerIdQuery(ownerId));

        // Assert
        assertEquals(expectedProperties, result);
        verify(propertyRepository).findPropertiesByOwnerId(ownerId);
    }

    @Test
    @DisplayName("GetAll: devuelve todas las propiedades")
    void handleGetAllPropertiesQuery_returnsAllProperties() {
        // Arrange
        var expectedProperties = List.of(property(new OwnerId(10L)), property(new OwnerId(20L)));
        when(propertyRepository.findAll()).thenReturn(expectedProperties);

        // Act
        var result = propertyQueryService.handle(new GetAllPropertiesQuery());

        // Assert
        assertEquals(expectedProperties, result);
        verify(propertyRepository).findAll();
    }

    private static Property property(OwnerId ownerId) {
        return new Property(new CreatePropertyCommand(
                ownerId,
                new Address("Calle Los Pinos", "123", "Lima", "15074", "Peru", -12.12f, -77.03f),
                new Region("Lima"),
                new District("Miraflores")
        ));
    }
}
