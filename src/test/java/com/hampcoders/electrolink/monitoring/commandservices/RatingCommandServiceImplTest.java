package com.hampcoders.electrolink.monitoring.commandservices;

import com.hampcoders.electrolink.monitoring.application.internal.commandservices.RatingCommandServiceImpl;
import com.hampcoders.electrolink.monitoring.domain.model.aggregates.Rating;
import com.hampcoders.electrolink.monitoring.domain.model.aggregates.ServiceOperation;
import com.hampcoders.electrolink.monitoring.domain.model.commands.AddRatingCommand;
import com.hampcoders.electrolink.monitoring.domain.model.commands.DeleteRatingCommand;
import com.hampcoders.electrolink.monitoring.domain.model.commands.UpdateRatingCommand;
import com.hampcoders.electrolink.monitoring.domain.model.valueObjects.*;
import com.hampcoders.electrolink.monitoring.infrastructure.persistence.jpa.repositories.RatingRepository;
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
class RatingCommandServiceImplTest {
  @Mock RatingRepository ratings;
  @Mock ServiceOperationRepository operations;
  @InjectMocks RatingCommandServiceImpl service;

  @Test
  @DisplayName("Crea una calificación para una operación completada")
  void createsRating() {
    // ARRANGE
    when(operations.findByRequestId(new RequestId(11L))).thenReturn(Optional.of(operation(ServiceStatus.COMPLETED)));
    var captured = ArgumentCaptor.forClass(Rating.class);

    // ACT
    service.handle(new AddRatingCommand(new RequestId(11L), 5, "Buen trabajo", "cliente", new TechnicianId(22L)));

    // ASSERT
    verify(ratings).save(captured.capture());
    assertEquals(new RequestId(11L), captured.getValue().getRequestId());
    assertEquals(5, captured.getValue().getScore());
    assertEquals("Buen trabajo", captured.getValue().getComment());
    assertEquals(new TechnicianId(22L), captured.getValue().getTechnicianId());
  }

  @Test
  @DisplayName("No califica una operación pendiente")
  void rejectsPendingOperation() {
    // ARRANGE
    when(operations.findByRequestId(new RequestId(11L))).thenReturn(Optional.of(operation(ServiceStatus.PENDING)));

    // ACT / ASSERT
    assertThrows(IllegalStateException.class, () -> service.handle(addCommand()));
    verify(ratings, never()).save(any());
  }

  @Test
  @DisplayName("No califica una solicitud sin operación")
  void rejectsMissingOperation() {
    // ARRANGE
    when(operations.findByRequestId(new RequestId(11L))).thenReturn(Optional.empty());

    // ACT / ASSERT
    assertThrows(IllegalArgumentException.class, () -> service.handle(addCommand()));
    verify(ratings, never()).save(any());
  }

  @Test
  @DisplayName("Actualiza puntuación y comentario")
  void updatesRating() {
    // ARRANGE
    var rating = rating();
    when(ratings.findById(new RatingId(4L))).thenReturn(Optional.of(rating));

    // ACT
    service.handle(new UpdateRatingCommand(new RatingId(4L), 4, "Actualizado"));

    // ASSERT
    assertEquals(4, rating.getScore());
    assertEquals("Actualizado", rating.getComment());
    verify(ratings).save(rating);
  }

  @Test
  @DisplayName("No actualiza una calificación inexistente")
  void updateMissingRating() {
    // ARRANGE
    when(ratings.findById(new RatingId(4L))).thenReturn(Optional.empty());

    // ACT / ASSERT
    assertThrows(IllegalArgumentException.class, () -> service.handle(new UpdateRatingCommand(new RatingId(4L), 4, "Actualizado")));
    verify(ratings, never()).save(any());
  }

  @Test
  @DisplayName("Elimina una calificación existente")
  void deletesRating() {
    // ARRANGE
    var rating = rating();
    when(ratings.findById(new RatingId(4L))).thenReturn(Optional.of(rating));

    // ACT
    service.handle(new DeleteRatingCommand(new RatingId(4L)));

    // ASSERT
    verify(ratings).delete(rating);
  }

  @Test
  @DisplayName("No elimina una calificación inexistente")
  void deleteMissingRating() {
    // ARRANGE
    when(ratings.findById(new RatingId(4L))).thenReturn(Optional.empty());

    // ACT / ASSERT
    assertThrows(IllegalArgumentException.class, () -> service.handle(new DeleteRatingCommand(new RatingId(4L))));
    verify(ratings, never()).delete(any());
  }

  private AddRatingCommand addCommand() {
    return new AddRatingCommand(new RequestId(11L), 5, "Bien", "cliente", new TechnicianId(22L));
  }

  private Rating rating() {
    return new Rating(new RequestId(11L), 3, "Inicial", "cliente", new TechnicianId(22L));
  }

  private ServiceOperation operation(ServiceStatus status) {
    return new ServiceOperation(new RequestId(11L), new TechnicianId(22L), OffsetDateTime.now(), null, status);
  }
}
