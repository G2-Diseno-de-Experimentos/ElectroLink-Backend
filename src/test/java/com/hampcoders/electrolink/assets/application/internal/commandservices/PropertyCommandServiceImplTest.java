package com.hampcoders.electrolink.assets.application.internal.commandservices;

import com.hampcoders.electrolink.assets.domain.model.aggregates.Property;
import com.hampcoders.electrolink.assets.domain.model.commands.CreatePropertyCommand;
import com.hampcoders.electrolink.assets.domain.model.commands.DeletePropertyCommand;
import com.hampcoders.electrolink.assets.domain.model.commands.UpdatePropertyCommand;
import com.hampcoders.electrolink.assets.domain.model.valueobjects.Address;
import com.hampcoders.electrolink.assets.domain.model.valueobjects.District;
import com.hampcoders.electrolink.assets.domain.model.valueobjects.OwnerId;
import com.hampcoders.electrolink.assets.domain.model.valueobjects.Region;
import com.hampcoders.electrolink.assets.infrastructure.persistence.jpa.repositories.PropertyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyCommandServiceImplTest {

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private PropertyCommandServiceImpl propertyCommandService;

    @Test
    @DisplayName("Create: guarda la propiedad y devuelve su identificador")
    void handleCreatePropertyCommand_whenCommandIsValid_savesPropertyAndReturnsId() {
        // Arrange
        var propertyId = UUID.randomUUID();
        var command = createPropertyCommand();
        var propertyCaptor = ArgumentCaptor.forClass(Property.class);
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> {
            Property property = invocation.getArgument(0);
            ReflectionTestUtils.setField(property, "id", propertyId);
            return property;
        });

        // Act
        var result = propertyCommandService.handle(command);

        // Assert
        verify(propertyRepository).save(propertyCaptor.capture());
        var savedProperty = propertyCaptor.getValue();
        assertEquals(propertyId, result);
        assertEquals(command.ownerId(), savedProperty.getOwnerId());
        assertEquals(command.address(), savedProperty.getAddress());
        assertEquals(command.region(), savedProperty.getRegion());
        assertEquals(command.district(), savedProperty.getDistrict());
    }

    @Test
    @DisplayName("Update: actualiza y guarda la propiedad cuando existe")
    void handleUpdatePropertyCommand_whenPropertyExists_updatesAndReturnsProperty() {
        // Arrange
        var propertyId = UUID.randomUUID();
        var property = new Property(createPropertyCommand());
        var command = new UpdatePropertyCommand(
                propertyId,
                address("Av. Javier Prado", "456"),
                new Region("Lima"),
                new District("San Isidro")
        );
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(propertyRepository.save(property)).thenReturn(property);

        // Act
        var result = propertyCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertSame(property, result.get());
        assertEquals(command.address(), property.getAddress());
        assertEquals(command.region(), property.getRegion());
        assertEquals(command.district(), property.getDistrict());
        verify(propertyRepository).save(property);
    }

    @Test
    @DisplayName("Update: devuelve Optional vacío cuando la propiedad no existe")
    void handleUpdatePropertyCommand_whenPropertyDoesNotExist_returnsEmpty() {
        // Arrange
        var propertyId = UUID.randomUUID();
        var command = new UpdatePropertyCommand(
                propertyId,
                address("Av. Javier Prado", "456"),
                new Region("Lima"),
                new District("San Isidro")
        );
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        // Act
        var result = propertyCommandService.handle(command);

        // Assert
        assertTrue(result.isEmpty());
        verify(propertyRepository, never()).save(any(Property.class));
    }

    @Test
    @DisplayName("Delete: devuelve true y guarda la propiedad cuando existe")
    void handleDeletePropertyCommand_whenPropertyExists_returnsTrue() {
        // Arrange
        var propertyId = UUID.randomUUID();
        var property = new Property(createPropertyCommand());
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(propertyRepository.save(property)).thenReturn(property);

        // Act
        var result = propertyCommandService.handle(new DeletePropertyCommand(propertyId));

        // Assert
        assertTrue(result);
        verify(propertyRepository).save(property);
    }

    @Test
    @DisplayName("Delete: devuelve false y no guarda cuando la propiedad no existe")
    void handleDeletePropertyCommand_whenPropertyDoesNotExist_returnsFalse() {
        // Arrange
        var propertyId = UUID.randomUUID();
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        // Act
        var result = propertyCommandService.handle(new DeletePropertyCommand(propertyId));

        // Assert
        assertFalse(result);
        verify(propertyRepository, never()).save(any(Property.class));
    }

    private static CreatePropertyCommand createPropertyCommand() {
        return new CreatePropertyCommand(
                new OwnerId(10L),
                address("Calle Los Pinos", "123"),
                new Region("Lima"),
                new District("Miraflores")
        );
    }

    private static Address address(String street, String number) {
        return new Address(street, number, "Lima", "15074", "Peru", -12.12f, -77.03f);
    }
}
