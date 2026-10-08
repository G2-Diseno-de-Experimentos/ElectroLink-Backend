package com.hampcoders.electrolink.monitoring.queryservices;

import com.hampcoders.electrolink.monitoring.application.internal.queryservices.RatingQueryServiceImpl;
import com.hampcoders.electrolink.monitoring.domain.model.aggregates.Rating;
import com.hampcoders.electrolink.monitoring.domain.model.queries.*;
import com.hampcoders.electrolink.monitoring.domain.model.valueObjects.RatingId;
import com.hampcoders.electrolink.monitoring.domain.model.valueObjects.RequestId;
import com.hampcoders.electrolink.monitoring.domain.model.valueObjects.TechnicianId;
import com.hampcoders.electrolink.monitoring.infrastructure.persistence.jpa.repositories.RatingRepository;
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
class RatingQueryServiceImplTest {
  @Mock RatingRepository repository;
  @InjectMocks RatingQueryServiceImpl service;

  @Test
  @DisplayName("Lista todas las calificaciones")
  void findsAll() {
    // ARRANGE
    var rating = mock(Rating.class);
    when(repository.findAll()).thenReturn(List.of(rating));

    // ACT / ASSERT
    assertEquals(List.of(rating), service.handle(new GetAllRatingsQuery()));
  }

  @Test
  @DisplayName("Busca una calificación por ID")
  void findsById() {
    // ARRANGE
    var rating = mock(Rating.class);
    when(repository.findById(new RatingId(4L))).thenReturn(Optional.of(rating));

    // ACT / ASSERT
    assertEquals(Optional.of(rating), service.handle(new GetRatingByIdQuery(4L)));
  }

  @Test
  @DisplayName("Devuelve vacío cuando no existe la calificación")
  void missingId() {
    // ARRANGE
    when(repository.findById(new RatingId(4L))).thenReturn(Optional.empty());

    // ACT / ASSERT
    assertTrue(service.handle(new GetRatingByIdQuery(4L)).isEmpty());
  }

  @Test
  @DisplayName("Lista calificaciones por request ID")
  void findsByRequest() {
    // ARRANGE
    var rating = mock(Rating.class);
    when(repository.findByRequestId(new RequestId(11L))).thenReturn(List.of(rating));

    // ACT / ASSERT
    assertEquals(List.of(rating), service.handle(new GetRatingsByRequestIdQuery(11L)));
  }

  @Test
  @DisplayName("Lista calificaciones por técnico")
  void findsByTechnician() {
    // ARRANGE
    var rating = mock(Rating.class);
    when(repository.findByTechnicianId(new TechnicianId(22L))).thenReturn(List.of(rating));

    // ACT / ASSERT
    assertEquals(List.of(rating), service.handle(new GetRatingsByTechnicianIdQuery(22L)));
  }
}
