package com.hampcoders.electrolink.assets.application.internal.queryservices;

import com.hampcoders.electrolink.assets.domain.model.aggregates.TechnicianInventory;
import com.hampcoders.electrolink.assets.domain.model.entities.ComponentStock;
import com.hampcoders.electrolink.assets.domain.model.queries.GetInventoriesWithLowStockQuery;
import com.hampcoders.electrolink.assets.domain.model.queries.GetInventoryByTechnicianIdQuery;
import com.hampcoders.electrolink.assets.domain.model.queries.GetStockItemDetailsQuery;
import com.hampcoders.electrolink.assets.domain.model.valueobjects.ComponentId;
import com.hampcoders.electrolink.assets.domain.model.valueobjects.TechnicianId;
import com.hampcoders.electrolink.assets.infrastructure.persistence.jpa.repositories.ComponentStockRepository;
import com.hampcoders.electrolink.assets.infrastructure.persistence.jpa.repositories.TechnicianInventoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechnicianInventoryQueryServiceImplTest {

    @Mock
    private TechnicianInventoryRepository technicianInventoryRepository;

    @Mock
    private ComponentStockRepository componentStockRepository;

    @InjectMocks
    private TechnicianInventoryQueryServiceImpl service;

    @Test
    @DisplayName("GetByTechnicianId: devuelve el inventario con sus stocks")
    void getInventoryByTechnicianId_whenExists_returnsInventory() {
        // Arrange
        var inventory = new TechnicianInventory(1L);
        when(technicianInventoryRepository.findByTechnicianIdWithStocks(1L)).thenReturn(Optional.of(inventory));

        // Act
        var result = service.handle(new GetInventoryByTechnicianIdQuery(new TechnicianId(1L)));

        // Assert
        assertThat(result).containsSame(inventory);
    }

    @Test
    @DisplayName("GetByTechnicianId: devuelve vacío si el técnico no tiene inventario")
    void getInventoryByTechnicianId_whenMissing_returnsEmpty() {
        // Arrange
        when(technicianInventoryRepository.findByTechnicianIdWithStocks(1L)).thenReturn(Optional.empty());

        // Act
        var result = service.handle(new GetInventoryByTechnicianIdQuery(new TechnicianId(1L)));

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("LowStock: devuelve los inventarios por debajo del umbral")
    void getInventoriesWithLowStock_returnsInventoriesBelowThreshold() {
        // Arrange
        var expected = List.of(new TechnicianInventory(1L), new TechnicianInventory(2L));
        when(technicianInventoryRepository.findInventoriesWithLowStock(5)).thenReturn(expected);

        // Act
        var result = service.handle(new GetInventoriesWithLowStockQuery(5));

        // Assert
        assertThat(result).containsExactlyElementsOf(expected);
    }

    @Test
    @DisplayName("LowStock: devuelve lista vacía si ningún inventario está bajo el umbral")
    void getInventoriesWithLowStock_whenNone_returnsEmptyList() {
        // Arrange
        when(technicianInventoryRepository.findInventoriesWithLowStock(5)).thenReturn(List.of());

        // Act
        var result = service.handle(new GetInventoriesWithLowStockQuery(5));

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("StockItemDetails: devuelve el stock del componente para el técnico")
    void getStockItemDetails_whenExists_returnsStock() {
        // Arrange
        var stock = new ComponentStock(new TechnicianInventory(1L), null, 8, 2, new Date());
        when(componentStockRepository.findByTechnicianInventoryIdAndComponentUid(1L, 20L))
                .thenReturn(Optional.of(stock));

        // Act
        var result = service.handle(new GetStockItemDetailsQuery(new TechnicianId(1L), new ComponentId(20L)));

        // Assert
        assertThat(result).containsSame(stock);
    }

    @Test
    @DisplayName("StockItemDetails: devuelve vacío si el técnico no tiene ese componente")
    void getStockItemDetails_whenMissing_returnsEmpty() {
        // Arrange
        when(componentStockRepository.findByTechnicianInventoryIdAndComponentUid(1L, 20L))
                .thenReturn(Optional.empty());

        // Act
        var result = service.handle(new GetStockItemDetailsQuery(new TechnicianId(1L), new ComponentId(20L)));

        // Assert
        assertThat(result).isEmpty();
    }
}
