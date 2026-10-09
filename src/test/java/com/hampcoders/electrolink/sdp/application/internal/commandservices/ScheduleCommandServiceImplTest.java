package com.hampcoders.electrolink.sdp.application.internal.commandservices;

import com.hampcoders.electrolink.sdp.domain.model.aggregates.ScheduleAggregate;
import com.hampcoders.electrolink.sdp.domain.model.commands.CreateScheduleCommand;
import com.hampcoders.electrolink.sdp.domain.model.commands.DeleteScheduleCommand;
import com.hampcoders.electrolink.sdp.domain.model.commands.UpdateScheduleCommand;
import com.hampcoders.electrolink.sdp.infrastructure.persistence.jpa.repositories.ScheduleRepository;
import com.hampcoders.electrolink.sdp.interfaces.rest.resources.CreateScheduleResource;
import com.hampcoders.electrolink.sdp.interfaces.rest.resources.UpdateScheduleResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleCommandServiceImplTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private ScheduleCommandServiceImpl service;

    // POST

    @Test
    @DisplayName("handle(CreateScheduleCommand) must be save and return the Schedule created")
    void handle_CreateScheduleCommand_MustReturnAndSaveSchedule() {
        // ARRANGE
        CreateScheduleResource resource = new CreateScheduleResource(
                "tech-123",
                "MONDAY",
                "08:00",
                "12:00"
        );
        CreateScheduleCommand command = new CreateScheduleCommand(
                resource.technicianId(),
                resource.day(),
                resource.startTime(),
                resource.endTime()
        );
        Long expectedId = 1L;
        ScheduleAggregate scheduleSaved = mock(ScheduleAggregate.class);

        // ACT
        when(scheduleSaved.getId()).thenReturn(expectedId);
        when(scheduleRepository.save(any(ScheduleAggregate.class))).thenReturn(scheduleSaved);
        var result = service.handle(command);

        //ASSERT
        assertNotNull(result, "El Schedule retornado no debe ser null");
        assertEquals(expectedId, result, "El Schedule retornado debe coincidir con el mock");
        verify(scheduleRepository, times(1)).save(any(ScheduleAggregate.class));
    }

    // UPDATE

    @Test
    @DisplayName("handle(UpdateScheduleCommand) must be return and update Schedule when already Exists")
    void handle_UpdateScheduleCommand_MustUpdateAndReturn_WhenExist(){
        // ARRANGE
        Long scheduleId = 1L;
        UpdateScheduleResource resource = new UpdateScheduleResource(
                "tech-123",
                "TUESDAY",
                "09:00",
                "13:00"
        );
        UpdateScheduleCommand command = new UpdateScheduleCommand(
                scheduleId,
                resource.technicianId(),
                resource.day(),
                resource.startTime(),
                resource.endTime()
        );
        ScheduleAggregate scheduleExist = mock(ScheduleAggregate.class);

        // ACT
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(scheduleExist));
        when(scheduleRepository.save(scheduleExist)).thenReturn(scheduleExist);
        service.handle(command);

        // ASSERT
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleRepository, times(1)).save(scheduleExist);
    }

    @Test
    @DisplayName("handle(UpdateScheduleCommand) must be throw IllegalArgumentException when Schedule doesn't exist")
    void handle_UpdateScheduleCommand_MustThrowException_WhenNotExist() {
        // ARRANGE
        Long scheduleId = 99L;
        UpdateScheduleResource resource = new UpdateScheduleResource(
                "tech-123",
                "TUESDAY",
                "09:00",
                "13:00"
        );
        UpdateScheduleCommand command = new UpdateScheduleCommand(
                scheduleId,
                resource.technicianId(),
                resource.day(),
                resource.startTime(),
                resource.endTime()
        );

        // ACT
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> service.handle(command),
                "Debe lanzar IllegalArgumentException cuando el id no sea encontrado."
        );

        // ASSERT
        assertEquals("Schedule not found", excepcion.getMessage());
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleRepository, never()).save(any());
    }

    // DELETE

    @Test
    @DisplayName("handle(DeleteScheduleCommand) must be delete Schedule when exist")
    void handle_DeleteScheduleCommand_MustDelete_WhenExist() {
        // ARRANGE
        Long scheduleId = 1L; // O el tipo de ID que utilice tu ScheduleId VO
        DeleteScheduleCommand command = new DeleteScheduleCommand(scheduleId);

        // ACT
        when(scheduleRepository.existsById(any())).thenReturn(true);
        service.handle(command);

        // ASSERT
        verify(scheduleRepository, times(1)).existsById(command.scheduleId());
        verify(scheduleRepository, times(1)).deleteById(command.scheduleId());
    }

    @Test
    @DisplayName("handle(DeleteScheduleCommand) must be throw IllegalArgumentException when Schedule not exist")
    void handle_DeleteScheduleCommand_MustThrowException_WhenNotExist() {
        // ARRANGE
        Long scheduleId = 80L;
        DeleteScheduleCommand command = new DeleteScheduleCommand(scheduleId);

        // ACT
        when(scheduleRepository.existsById(scheduleId)).thenReturn(false);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.handle(command),
                "Debe lanzar IllegalArgumentException cuando intente eliminar un id inexistente."
        );

        // ASSERT
        assertEquals("Schedule not found", exception.getMessage());
        verify(scheduleRepository, times(1)).existsById(scheduleId);
        verify(scheduleRepository, never()).deleteById(any());
    }
}