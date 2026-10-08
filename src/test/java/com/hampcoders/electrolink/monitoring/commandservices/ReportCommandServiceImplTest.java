package com.hampcoders.electrolink.monitoring.commandservices;

import com.hampcoders.electrolink.monitoring.application.internal.commandservices.ReportCommandServiceImpl;
import com.hampcoders.electrolink.monitoring.domain.model.aggregates.Report;
import com.hampcoders.electrolink.monitoring.domain.model.aggregates.ServiceOperation;
import com.hampcoders.electrolink.monitoring.domain.model.commands.AddPhotoCommand;
import com.hampcoders.electrolink.monitoring.domain.model.commands.AddReportCommand;
import com.hampcoders.electrolink.monitoring.domain.model.commands.DeleteReportCommand;
import com.hampcoders.electrolink.monitoring.domain.model.entities.ReportPhoto;
import com.hampcoders.electrolink.monitoring.domain.model.valueObjects.*;
import com.hampcoders.electrolink.monitoring.infrastructure.persistence.jpa.repositories.ReportRepository;
import com.hampcoders.electrolink.monitoring.infrastructure.persistence.jpa.repositories.ServiceOperationRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportCommandServiceImplTest {
  @Mock ReportRepository reports;
  @Mock EntityManager entityManager;
  @Mock ServiceOperationRepository operations;
  @InjectMocks ReportCommandServiceImpl service;

  @Test
  @DisplayName("Crea un reporte cuando existe la operación")
  void createsReport() {
    // ARRANGE
    when(operations.findByRequestId(new RequestId(11L))).thenReturn(Optional.of(mock(ServiceOperation.class)));
    var captured = ArgumentCaptor.forClass(Report.class);

    // ACT
    service.handle(new AddReportCommand(new RequestId(11L), ReportType.INCIDENT, "Falla eléctrica"));

    // ASSERT
    verify(reports).save(captured.capture());
    assertEquals(new RequestId(11L), captured.getValue().getRequestId());
    assertEquals(ReportType.INCIDENT, captured.getValue().getReportType());
    assertEquals("Falla eléctrica", captured.getValue().getDescription());
  }

  @Test
  @DisplayName("No crea un reporte sin operación")
  void rejectsMissingOperation() {
    // ARRANGE
    when(operations.findByRequestId(new RequestId(11L))).thenReturn(Optional.empty());

    // ACT / ASSERT
    assertThrows(IllegalArgumentException.class, () -> service.handle(new AddReportCommand(new RequestId(11L), ReportType.INCIDENT, "Falla")));
    verify(reports, never()).save(any());
  }

  @Test
  @DisplayName("Elimina un reporte existente")
  void deletesReport() {
    // ARRANGE
    var report = new Report(new RequestId(11L), ReportType.COMPLETION, "Listo");
    when(reports.findById(3L)).thenReturn(Optional.of(report));

    // ACT
    service.handle(new DeleteReportCommand(3L));

    // ASSERT
    verify(reports).delete(report);
  }

  @Test
  @DisplayName("No elimina un reporte inexistente")
  void deleteMissingReport() {
    // ARRANGE
    when(reports.findById(3L)).thenReturn(Optional.empty());

    // ACT / ASSERT
    assertThrows(IllegalArgumentException.class, () -> service.handle(new DeleteReportCommand(3L)));
    verify(reports, never()).delete(any());
  }

  @Test
  @DisplayName("Persiste una foto con el reporte indicado")
  void addsPhoto() {
    // ARRANGE
    var photoId = new ReportPhotoId(9L);
    var captured = ArgumentCaptor.forClass(ReportPhoto.class);

    // ACT
    var result = service.handle(new AddPhotoCommand(photoId, new ReportId(3L), "https://example.org/photo.jpg"));

    // ASSERT
    verify(entityManager).persist(captured.capture());
    assertEquals(photoId, result);
    assertEquals(new ReportId(3L), captured.getValue().getReportId());
    assertEquals("https://example.org/photo.jpg", captured.getValue().getUrl());
  }
}
