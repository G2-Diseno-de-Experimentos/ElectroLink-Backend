package com.hampcoders.electrolink.sdp.application.internal.queryservices;
import com.hampcoders.electrolink.sdp.domain.model.aggregates.ServiceEntity;
import com.hampcoders.electrolink.sdp.domain.model.queries.FindServiceByIdQuery;
import com.hampcoders.electrolink.sdp.domain.model.queries.GetAllServicesQuery;
import com.hampcoders.electrolink.sdp.infrastructure.persistence.jpa.repositories.ServiceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServiceQueryServiceImplTest {
    @Test
    @DisplayName("handle(FindServiceByIdQuery) should return service when found")
    void handle_FindServiceByIdQuery_ReturnsService_WhenFound() {
        // ARRANGE
        ServiceRepository serviceRepository = mock(ServiceRepository.class);
        ServiceQueryServiceImpl queryService = new ServiceQueryServiceImpl(serviceRepository);

        FindServiceByIdQuery query = mock(FindServiceByIdQuery.class);
        when(query.serviceId()).thenReturn(1L);

        ServiceEntity expectedService = mock(ServiceEntity.class);
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(expectedService));

        // ACT
        Optional<ServiceEntity> result = queryService.handle(query);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals(expectedService, result.get());
        verify(serviceRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("handle(GetAllServicesQuery) should return all services")
    void handle_GetAllServicesQuery_ReturnsAllServices() {
        // ARRANGE
        ServiceRepository serviceRepository = mock(ServiceRepository.class);
        ServiceQueryServiceImpl queryService = new ServiceQueryServiceImpl(serviceRepository);

        GetAllServicesQuery query = mock(GetAllServicesQuery.class);
        ServiceEntity service1 = mock(ServiceEntity.class);
        ServiceEntity service2 = mock(ServiceEntity.class);

        when(serviceRepository.findAll()).thenReturn(List.of(service1, service2));

        // ACT
        List<ServiceEntity> result = queryService.handle(query);

        // ASSERT
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(serviceRepository, times(1)).findAll();
    }
}
