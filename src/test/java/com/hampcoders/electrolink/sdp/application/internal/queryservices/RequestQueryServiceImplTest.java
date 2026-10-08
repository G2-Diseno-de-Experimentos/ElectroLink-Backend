package com.hampcoders.electrolink.sdp.application.internal.queryservices;

import com.hampcoders.electrolink.sdp.domain.model.aggregates.Request;
import com.hampcoders.electrolink.sdp.domain.model.queries.FindRequestByIdQuery;
import com.hampcoders.electrolink.sdp.domain.model.queries.FindRequestsByClientIdQuery;
import com.hampcoders.electrolink.sdp.infrastructure.persistence.jpa.repositories.RequestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestQueryServiceImplTest {
    @Test
    @DisplayName("handle(FindRequestByIdQuery) should return request when found")
    void handle_FindRequestByIdQuery_ReturnsRequest_WhenFound() {
        // ARRANGE
        RequestRepository requestRepository = mock(RequestRepository.class);
        RequestQueryServiceImpl queryService = new RequestQueryServiceImpl(requestRepository);

        FindRequestByIdQuery query = mock(FindRequestByIdQuery.class);
        when(query.requestId()).thenReturn(1L);

        Request expectedRequest = mock(Request.class);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(expectedRequest));

        // ACT
        Optional<Request> result = queryService.handle(query);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals(expectedRequest, result.get());
        verify(requestRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("handle(FindRequestsByClientIdQuery) should return list of requests for client")
    void handle_FindRequestsByClientIdQuery_ReturnsList() {
        // ARRANGE
        RequestRepository requestRepository = mock(RequestRepository.class);
        RequestQueryServiceImpl queryService = new RequestQueryServiceImpl(requestRepository);

        FindRequestsByClientIdQuery query = mock(FindRequestsByClientIdQuery.class);
        when(query.clientId()).thenReturn("100L");

        Request request = mock(Request.class);
        when(requestRepository.findByClientId("100L")).thenReturn(List.of(request));

        // ACT
        List<Request> result = queryService.handle(query);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(requestRepository, times(1)).findByClientId("100L");
    }
}
