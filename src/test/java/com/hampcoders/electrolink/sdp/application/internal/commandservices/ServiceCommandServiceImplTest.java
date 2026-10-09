package com.hampcoders.electrolink.sdp.application.internal.commandservices;

import com.hampcoders.electrolink.sdp.domain.model.aggregates.ServiceEntity;
import com.hampcoders.electrolink.sdp.domain.model.commands.CreateServiceCommand;
import com.hampcoders.electrolink.sdp.domain.model.commands.DeleteServiceCommand;
import com.hampcoders.electrolink.sdp.domain.model.commands.UpdateServiceCommand;
import com.hampcoders.electrolink.sdp.domain.model.valueobjects.Policy;
import com.hampcoders.electrolink.sdp.domain.model.valueobjects.Restriction;
import com.hampcoders.electrolink.sdp.infrastructure.persistence.jpa.repositories.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServiceCommandServiceImplTest {
    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private ServiceCommandServiceImpl serviceCommandService;

    private CreateServiceCommand createCommand;
    private UpdateServiceCommand updateCommand;
    private DeleteServiceCommand deleteCommand;

    @BeforeEach
    void setUp() {
        Policy policy = new Policy("Política de cancelación estándar", "Términos y condiciones del servicio");
        Restriction restriction = new Restriction(Collections.emptyList(), Collections.emptyList(), false);

        createCommand = new CreateServiceCommand(
                "Mantenimiento Preventivo",
                "Descripción del servicio",
                150.0,
                "2 horas",
                "Mantenimiento",
                true,
                "Admin",
                policy,
                restriction,
                Collections.emptyList(),
                Collections.emptyList()
        );

        updateCommand = new UpdateServiceCommand(
                1L,
                "Mantenimiento Correctivo",
                "Descripción actualizada",
                200.0,
                "3 horas",
                "Mantenimiento",
                true,
                "Admin",
                policy,
                restriction,
                Collections.emptyList(),
                Collections.emptyList()
        );

        deleteCommand = new DeleteServiceCommand(1L);
    }

    // POST

    @Test
    @DisplayName("handle(CreateServiceCommand) should save entity and return ID when command is valid")
    void handle_CreateServiceCommand_Success() {
        // ARRANGE
        ServiceEntity savedEntity = mock(ServiceEntity.class);
        when(savedEntity.getId()).thenReturn(10L);
        when(serviceRepository.save(any(ServiceEntity.class))).thenReturn(savedEntity);

        // ACT
        Long resultId = serviceCommandService.handle(createCommand);

        // ASSERT
        assertNotNull(resultId);
        assertEquals(10L, resultId);
        verify(serviceRepository, times(1)).save(any(ServiceEntity.class));
    }

    // UPDATE

    @Test
    @DisplayName("handle(UpdateServiceCommand) should update and save entity when service exists")
    void handle_UpdateServiceCommand_Success() {
        // ARRANGE
        ServiceEntity existingEntity = mock(ServiceEntity.class);
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(serviceRepository.save(existingEntity)).thenReturn(existingEntity);

        // ACT
        assertDoesNotThrow(() -> serviceCommandService.handle(updateCommand));

        // ASSERT
        verify(serviceRepository, times(1)).findById(1L);
        verify(existingEntity, times(1)).updateFrom(any(ServiceEntity.class));
        verify(serviceRepository, times(1)).save(existingEntity);
    }

    @Test
    @DisplayName("handle(UpdateServiceCommand) should throw IllegalArgumentException when service does not exist")
    void handle_UpdateServiceCommand_ThrowsException_WhenNotExist() {
        // ARRANGE
        when(serviceRepository.findById(1L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> serviceCommandService.handle(updateCommand)
        );

        assertEquals("Service not found with id: 1", exception.getMessage());
        verify(serviceRepository, times(1)).findById(1L);
        verify(serviceRepository, never()).save(any());
    }

    // DELETE

    @Test
    @DisplayName("handle(DeleteServiceCommand) should delete service when it exists")
    void handle_DeleteServiceCommand_Success() {
        // ARRANGE
        when(serviceRepository.existsById(1L)).thenReturn(true);

        // ACT
        assertDoesNotThrow(() -> serviceCommandService.handle(deleteCommand));

        // ASSERT
        verify(serviceRepository, times(1)).existsById(1L);
        verify(serviceRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("handle(DeleteServiceCommand) should throw IllegalArgumentException when service does not exist")
    void handle_DeleteServiceCommand_ThrowsException_WhenNotExist() {
        // ARRANGE
        when(serviceRepository.existsById(1L)).thenReturn(false);

        // ACT & ASSERT
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> serviceCommandService.handle(deleteCommand)
        );

        assertEquals("Service not found with id: 1", exception.getMessage());
        verify(serviceRepository, times(1)).existsById(1L);
        verify(serviceRepository, never()).deleteById(any());
    }
}
