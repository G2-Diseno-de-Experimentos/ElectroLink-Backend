package com.hampcoders.electrolink.sdp.application.internal.commandservices;

import com.hampcoders.electrolink.sdp.domain.model.aggregates.Request;
import com.hampcoders.electrolink.sdp.domain.model.commands.CreateRequestCommand;
import com.hampcoders.electrolink.sdp.domain.model.commands.DeleteRequestCommand;
import com.hampcoders.electrolink.sdp.domain.model.commands.UpdateRequestCommand;
import com.hampcoders.electrolink.sdp.infrastructure.persistence.jpa.repositories.RequestRepository;
import com.hampcoders.electrolink.sdp.interfaces.rest.resources.CreateRequestResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestCommandServiceImplTest {
    @Mock
    private RequestRepository requestRepository;

    @InjectMocks
    private RequestCommandServiceImpl service;

    // POST

    @Test
    @DisplayName("handle(CreateRequestCommand) must return and save the created Request")
    void handle_CreateRequestCommand_MustReturnAndSaveRequest(){
        // ARRANGE
        CreateRequestResource resource = new CreateRequestResource(
                "client-123",
                "tech-456",
                "prop-789",
                "serv-101",
                "Falla eléctrica en panel principal",
                LocalDate.now(),
                new CreateRequestResource.BillResource("2026-10", 150.5, 200.0, "https://example.com/bill.jpg"),
                Collections.emptyList()
        );
        CreateRequestCommand command = new CreateRequestCommand(resource);
        Request requestSaved = mock(Request.class);

        // ACT
        when(requestRepository.save(any(Request.class))).thenReturn(requestSaved);
        Request result = service.handle(command);

        // ASSERT
        assertNotNull(result, "El objeto Request retornado no debe ser null.");
        assertEquals(requestSaved, result, "El Request devuelto debe coincidir con el simulado.");
        verify(requestRepository, times(1)).save(any(Request.class));
    }

    // UPDATE

    @Test
    @DisplayName("handle(UpdateRequestCommand) must throw IllegalArgumentException when request doesn't exist")
    void handle_UpdateRequestCommand_MustThrowException_WhenRequestNotExist(){
        // ARRANGE
        Long requestId = 99L;
        UpdateRequestCommand command = mock(UpdateRequestCommand.class);

        // ACT
        when(command.requestId()).thenReturn(requestId);
        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.handle(command),
                "Debe lanzar IllegalArgumentException cuando el id no sea encontrado."
        );

        // ASSERT
        assertEquals("Request not found", exception.getMessage());
        verify(requestRepository, times(1)).findById(requestId);
        verify(requestRepository, never()).save(any());
    }

    // DELETE

    @Test
    @DisplayName("handle(DeleteRequestCommand) must delete request when exits")
    void handle_DeleteRequestCommand_MustDelete_WhenExist() {
        // ARRANGE
        Long requestId = 1L;
        DeleteRequestCommand command = mock(DeleteRequestCommand.class);
        Request requestExist = mock(Request.class);

        // ACT
        when(command.requestId()).thenReturn(requestId);
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(requestExist));
        service.handle(command);

        // ASSERT
        verify(requestRepository, times(1)).findById(requestId);
        verify(requestRepository, times(1)).delete(requestExist);
    }

    @Test
    @DisplayName("handle(DeleteRequestCommand) must be throw IllegalArgumentException when request doesn't exist")
    void handle_DeleteRequestCommand_MustThrowException_WhenNotExist() {
        // ARRANGE
        Long requestId = 99L;
        DeleteRequestCommand command = mock(DeleteRequestCommand.class);

        // ACT
        when(command.requestId()).thenReturn(requestId);
        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.handle(command),
                "Debe lanzar IllegalArgumentException cuando intente eliminar un id inexistente."
        );

        // ASSERT
        assertEquals("Request not found", exception.getMessage());
        verify(requestRepository, times(1)).findById(requestId);
        verify(requestRepository, never()).delete(any());
    }
}
