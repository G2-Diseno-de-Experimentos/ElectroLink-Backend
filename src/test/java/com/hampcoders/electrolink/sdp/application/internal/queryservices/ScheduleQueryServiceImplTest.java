package com.hampcoders.electrolink.sdp.application.internal.queryservices;

import com.hampcoders.electrolink.sdp.domain.model.aggregates.ScheduleAggregate;
import com.hampcoders.electrolink.sdp.domain.model.queries.FindScheduleByIdQuery;
import com.hampcoders.electrolink.sdp.domain.model.queries.FindSchedulesByTechnicianIdQuery;
import com.hampcoders.electrolink.sdp.infrastructure.persistence.jpa.repositories.ScheduleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleQueryServiceImplTest {
    @Test
    @DisplayName("handle(FindScheduleByIdQuery) should return schedule when found")
    void handle_FindScheduleByIdQuery_ReturnsSchedule_WhenFound() {
        // ARRANGE
        ScheduleRepository scheduleRepository = mock(ScheduleRepository.class);
        ScheduleQueryServiceImpl queryService = new ScheduleQueryServiceImpl(scheduleRepository);

        FindScheduleByIdQuery query = mock(FindScheduleByIdQuery.class);
        when(query.scheduleId()).thenReturn(1L);

        ScheduleAggregate expectedSchedule = mock(ScheduleAggregate.class);
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(expectedSchedule));

        // ACT
        Optional<ScheduleAggregate> result = queryService.handle(query);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals(expectedSchedule, result.get());
        verify(scheduleRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("handle(FindSchedulesByTechnicianIdQuery) should return list of schedules for technician")
    void handle_FindSchedulesByTechnicianIdQuery_ReturnsList() {
        // ARRANGE
        ScheduleRepository scheduleRepository = mock(ScheduleRepository.class);
        ScheduleQueryServiceImpl queryService = new ScheduleQueryServiceImpl(scheduleRepository);

        FindSchedulesByTechnicianIdQuery query = mock(FindSchedulesByTechnicianIdQuery.class);
        when(query.technicianId()).thenReturn("50L");

        ScheduleAggregate schedule = mock(ScheduleAggregate.class);
        when(scheduleRepository.findByTechnicianId("50L")).thenReturn(List.of(schedule));

        // ACT
        List<ScheduleAggregate> result = queryService.handle(query);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(scheduleRepository, times(1)).findByTechnicianId("50L");
    }
}
