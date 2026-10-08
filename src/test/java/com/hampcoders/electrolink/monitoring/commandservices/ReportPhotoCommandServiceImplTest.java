package com.hampcoders.electrolink.monitoring.commandservices;

import com.hampcoders.electrolink.monitoring.application.internal.commandservices.ReportPhotoCommandServiceImpl;
import com.hampcoders.electrolink.monitoring.domain.model.aggregates.Report;
import com.hampcoders.electrolink.monitoring.domain.model.commands.AddPhotoCommand;
import com.hampcoders.electrolink.monitoring.domain.model.entities.ReportPhoto;
import com.hampcoders.electrolink.monitoring.domain.model.valueObjects.ReportId;
import com.hampcoders.electrolink.monitoring.domain.model.valueObjects.ReportPhotoId;
import com.hampcoders.electrolink.monitoring.infrastructure.persistence.jpa.repositories.ReportPhotoRepository;
import com.hampcoders.electrolink.monitoring.infrastructure.persistence.jpa.repositories.ReportRepository;
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
class ReportPhotoCommandServiceImplTest {
  @Mock ReportRepository reports;
  @Mock ReportPhotoRepository photos;
  @InjectMocks ReportPhotoCommandServiceImpl service;

  @Test
  @DisplayName("Asocia una foto con un reporte existente")
  void addsPhoto() {
    // ARRANGE
    when(reports.findById(new ReportId(3L))).thenReturn(Optional.of(mock(Report.class)));
    var captured = ArgumentCaptor.forClass(ReportPhoto.class);
    when(photos.save(any(ReportPhoto.class))).thenAnswer(invocation -> {
      ReportPhoto photo = invocation.getArgument(0);
      var id = ReportPhotoId.class.getDeclaredField("id");
      id.setAccessible(true);
      id.set(photo.getId(), 9L);
      return photo;
    });

    // ACT
    var id = service.handle(new AddPhotoCommand(new ReportPhotoId(), new ReportId(3L), "https://example.org/photo.jpg"));

    // ASSERT
    verify(photos).save(captured.capture());
    assertEquals(9L, id);
    assertEquals(new ReportId(3L), captured.getValue().getReportId());
    assertEquals("https://example.org/photo.jpg", captured.getValue().getUrl());
  }

  @Test
  @DisplayName("No asocia una foto a un reporte inexistente")
  void missingReport() {
    // ARRANGE
    when(reports.findById(new ReportId(3L))).thenReturn(Optional.empty());

    // ACT / ASSERT
    assertThrows(IllegalArgumentException.class, () -> service.handle(new AddPhotoCommand(new ReportPhotoId(), new ReportId(3L), "url")));
    verify(photos, never()).save(any());
  }
}
