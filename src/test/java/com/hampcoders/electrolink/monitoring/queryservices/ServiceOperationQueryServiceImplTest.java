package com.hampcoders.electrolink.monitoring.queryservices;

import com.hampcoders.electrolink.assets.domain.model.valueobjects.TechnicianId;
import com.hampcoders.electrolink.monitoring.application.internal.queryservices.ServiceOperationQueryServiceImpl;
import com.hampcoders.electrolink.monitoring.domain.model.aggregates.ServiceOperation;
import com.hampcoders.electrolink.monitoring.domain.model.queries.*;
import com.hampcoders.electrolink.monitoring.domain.model.valueObjects.RequestId;
import com.hampcoders.electrolink.monitoring.infrastructure.persistence.jpa.repositories.ServiceOperationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceOperationQueryServiceImplTest {
  @Mock ServiceOperationRepository repository;
  @InjectMocks ServiceOperationQueryServiceImpl service;

  @Test
  @DisplayName("Lista todas las operaciones")
  void findsAll() {
    // ARRANGE
    var operation = mock(ServiceOperation.class);
    when(repository.findAll()).thenReturn(List.of(operation));

    // ACT / ASSERT
    assertEquals(List.of(operation), service.handle(new GetAllServiceOperationsQuery()));
  }

  @Test
  @DisplayName("Busca una operación por request ID")
  void findsById() {
    // ARRANGE
    var operation = mock(ServiceOperation.class);
    when(repository.findByRequestId(new RequestId(11L))).thenReturn(Optional.of(operation));

    // ACT / ASSERT
    assertEquals(Optional.of(operation), service.handle(new GetServiceOperationByIdQuery(11L)));
  }

  @Test
  @DisplayName("Devuelve vacío cuando no existe la operación")
  void missingId() {
    // ARRANGE
    when(repository.findByRequestId(new RequestId(11L))).thenReturn(Optional.empty());

    // ACT / ASSERT
    assertTrue(service.handle(new GetServiceOperationByIdQuery(11L)).isEmpty());
  }

  @Test
  @DisplayName("Lista operaciones de un técnico")
  void findsByTechnician() {
    // ARRANGE
    var operation = mock(ServiceOperation.class);
    when(repository.findByTechnicianId(new TechnicianId(22L))).thenReturn(List.of(operation));

    // ACT / ASSERT
    assertEquals(List.of(operation), service.handle(new GetServiceOperationsByTechnicianIdQuery(22L)));
  }
}
