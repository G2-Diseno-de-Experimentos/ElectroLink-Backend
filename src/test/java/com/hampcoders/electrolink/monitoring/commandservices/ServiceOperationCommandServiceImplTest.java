package com.hampcoders.electrolink.monitoring.commandservices;

import com.hampcoders.electrolink.monitoring.application.internal.commandservices.ServiceOperationCommandServiceImpl;
import com.hampcoders.electrolink.monitoring.domain.model.aggregates.ServiceOperation;
import com.hampcoders.electrolink.monitoring.domain.model.commands.CreateServiceOperationCommand;
import com.hampcoders.electrolink.monitoring.domain.model.commands.UpdateServiceStatusCommand;
import com.hampcoders.electrolink.monitoring.domain.model.valueObjects.RequestId;
import com.hampcoders.electrolink.monitoring.domain.model.valueObjects.ServiceStatus;
import com.hampcoders.electrolink.monitoring.domain.model.valueObjects.TechnicianId;
import com.hampcoders.electrolink.monitoring.infrastructure.persistence.jpa.repositories.ServiceOperationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceOperationCommandServiceImplTest {
  @Mock ServiceOperationRepository repository;
  @InjectMocks ServiceOperationCommandServiceImpl service;

  @Test
  @DisplayName("Crea una operación con los datos del comando")
  void createsOperation() {
    // ARRANGE
    var started = OffsetDateTime.now().minusHours(1);
    var command = new CreateServiceOperationCommand(new RequestId(11L), new TechnicianId(22L), started, null, ServiceStatus.IN_PROGRESS);
    var saved = ArgumentCaptor.forClass(ServiceOperation.class);

    // ACT
    var id = service.handle(command);

    // ASSERT
    verify(repository).save(saved.capture());
    assertEquals(new RequestId(11L), id);
    assertEquals(id, saved.getValue().getRequestId());
    assertEquals(new TechnicianId(22L), saved.getValue().getTechnicianId());
    assertEquals(started, saved.getValue().getStartedAt());
    assertEquals(ServiceStatus.IN_PROGRESS, saved.getValue().getStatus());
  }

  @Test
  @DisplayName("Al completar asigna completedAt y guarda la operación")
  void completesOperation() {
    // ARRANGE
    var operation = operation();
    when(repository.findByRequestId(new RequestId(11L))).thenReturn(Optional.of(operation));
    var before = OffsetDateTime.now();

    // ACT
    service.handle(new UpdateServiceStatusCommand(11L, "COMPLETED"));

    // ASSERT
    assertEquals(ServiceStatus.COMPLETED, operation.getStatus());
    assertNotNull(operation.getCompletedAt());
    assertFalse(operation.getCompletedAt().isBefore(before));
    assertFalse(operation.getCompletedAt().isAfter(OffsetDateTime.now()));
    verify(repository).save(operation);
  }

  @Test
  @DisplayName("Un cambio a IN_PROGRESS no asigna completedAt")
  void updatesWithoutCompleting() {
    // ARRANGE
    var operation = operation();
    when(repository.findByRequestId(new RequestId(11L))).thenReturn(Optional.of(operation));

    // ACT
    service.handle(new UpdateServiceStatusCommand(11L, "IN_PROGRESS"));

    // ASSERT
    assertEquals(ServiceStatus.IN_PROGRESS, operation.getStatus());
    assertNull(operation.getCompletedAt());
    verify(repository).save(operation);
  }

  @Test
  @DisplayName("Rechaza la actualización de una operación inexistente")
  void missingOperation() {
    // ARRANGE
    when(repository.findByRequestId(new RequestId(11L))).thenReturn(Optional.empty());

    // ACT / ASSERT
    assertThrows(IllegalArgumentException.class, () -> service.handle(new UpdateServiceStatusCommand(11L, "COMPLETED")));
    verify(repository, never()).save(any());
  }

  @Test
  @DisplayName("Rechaza un estado desconocido sin guardar")
  void invalidStatus() {
    // ARRANGE
    var operation = operation();
    when(repository.findByRequestId(new RequestId(11L))).thenReturn(Optional.of(operation));

    // ACT / ASSERT
    assertThrows(IllegalArgumentException.class, () -> service.handle(new UpdateServiceStatusCommand(11L, "UNKNOWN")));
    assertEquals(ServiceStatus.PENDING, operation.getStatus());
    verify(repository, never()).save(any());
  }

  private ServiceOperation operation() {
    return new ServiceOperation(new RequestId(11L), new TechnicianId(22L), OffsetDateTime.now().minusHours(1), null, ServiceStatus.PENDING);
  }
}
