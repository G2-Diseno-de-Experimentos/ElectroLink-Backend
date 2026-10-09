package com.hampcoders.electrolink.monitoring.queryservices;

import com.hampcoders.electrolink.monitoring.application.internal.queryservices.ReportQueryServiceImpl;
import com.hampcoders.electrolink.monitoring.domain.model.aggregates.Report;
import com.hampcoders.electrolink.monitoring.domain.model.queries.*;
import com.hampcoders.electrolink.monitoring.domain.model.valueObjects.ReportId;
import com.hampcoders.electrolink.monitoring.domain.model.valueObjects.RequestId;
import com.hampcoders.electrolink.monitoring.infrastructure.persistence.jpa.repositories.ReportRepository;
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
class ReportQueryServiceImplTest {
  @Mock ReportRepository repository;
  @InjectMocks ReportQueryServiceImpl service;

  @Test
  @DisplayName("Lista todos los reportes")
  void findsAll() {
    // ARRANGE
    var report = mock(Report.class);
    when(repository.findAll()).thenReturn(List.of(report));

    // ACT / ASSERT
    assertEquals(List.of(report), service.handle(new GetAllReportsQuery()));
  }

  @Test
  @DisplayName("Busca un reporte por ID")
  void findsById() {
    // ARRANGE
    var report = mock(Report.class);
    when(repository.findById(new ReportId(3L))).thenReturn(Optional.of(report));

    // ACT / ASSERT
    assertEquals(Optional.of(report), service.handle(new GetReportByIdQuery(3L)));
  }

  @Test
  @DisplayName("Devuelve vacío cuando no existe el reporte")
  void missingId() {
    // ARRANGE
    when(repository.findById(new ReportId(3L))).thenReturn(Optional.empty());

    // ACT / ASSERT
    assertTrue(service.handle(new GetReportByIdQuery(3L)).isEmpty());
  }

  @Test
  @DisplayName("Lista reportes por request ID")
  void findsByRequest() {
    // ARRANGE
    var report = mock(Report.class);
    when(repository.findByRequestId(new RequestId(11L))).thenReturn(List.of(report));

    // ACT / ASSERT
    assertEquals(List.of(report), service.handle(new GetReportsByRequestIdQuery(11L)));
  }
}
