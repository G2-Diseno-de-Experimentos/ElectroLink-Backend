package com.hampcoders.electrolink.assets.application.internal.commandservices;

import com.hampcoders.electrolink.assets.domain.model.aggregates.Component;
import com.hampcoders.electrolink.assets.domain.model.aggregates.TechnicianInventory;
import com.hampcoders.electrolink.assets.domain.model.commands.AddComponentStockCommand;
import com.hampcoders.electrolink.assets.domain.model.commands.CreateComponentCommand;
import com.hampcoders.electrolink.assets.domain.model.commands.CreateTechnicianInventoryCommand;
import com.hampcoders.electrolink.assets.domain.model.commands.DeleteComponentStockCommand;
import com.hampcoders.electrolink.assets.domain.model.commands.UpdateComponentStockCommand;
import com.hampcoders.electrolink.assets.domain.model.valueobjects.TechnicianId;
import com.hampcoders.electrolink.assets.infrastructure.persistence.jpa.repositories.ComponentRepository;
import com.hampcoders.electrolink.assets.infrastructure.persistence.jpa.repositories.TechnicianInventoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechnicianInventoryCommandServiceImplTest {

    private static final Long TECHNICIAN_ID = 1L;
    private static final Long COMPONENT_ID = 20L;

    @Mock
    private TechnicianInventoryRepository technicianInventoryRepository;

    @Mock
    private ComponentRepository componentRepository;

    @InjectMocks
    private TechnicianInventoryCommandServiceImpl service;

    private static Component componentWithUid(Long uid) {
        var component = new Component(new CreateComponentCommand(UUID.randomUUID(), "Breaker", "desc", 1L, true));
        ReflectionTestUtils.setField(component, "componentUid", uid);
        return component;
    }

    private static TechnicianInventory inventoryWithStock(int quantity, int threshold) {
        var inventory = new TechnicianInventory(TECHNICIAN_ID);
        inventory.addToStock(componentWithUid(COMPONENT_ID), quantity, threshold);
        return inventory;
    }

    // ---------- CreateTechnicianInventoryCommand ----------

    @Test
    @DisplayName("Create: guarda un inventario nuevo para el técnico")
    void createInventory_whenTechnicianHasNone_savesInventory() {
        // Arrange
        var command = new CreateTechnicianInventoryCommand(new TechnicianId(TECHNICIAN_ID));
        when(technicianInventoryRepository.existsByTechnicianId(TECHNICIAN_ID)).thenReturn(false);

        // Act
        service.handle(command);

        // Assert
        var captor = ArgumentCaptor.forClass(TechnicianInventory.class);
        verify(technicianInventoryRepository).save(captor.capture());
        assertThat(captor.getValue().getTechnicianId()).isEqualTo(TECHNICIAN_ID);
        assertThat(captor.getValue().getComponentStocks()).isEmpty();
    }

    @Test
    @DisplayName("Create: lanza IllegalStateException si el técnico ya tiene inventario")
    void createInventory_whenTechnicianAlreadyHasOne_throwsIllegalStateException() {
        // Arrange
        var command = new CreateTechnicianInventoryCommand(new TechnicianId(TECHNICIAN_ID));
        when(technicianInventoryRepository.existsByTechnicianId(TECHNICIAN_ID)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Technician inventory already exists for this technician ID");
        verify(technicianInventoryRepository, never()).save(any());
    }

    // ---------- AddComponentStockCommand ----------

    @Test
    @DisplayName("AddStock: agrega el componente al inventario con cantidad y umbral")
    void addStock_whenInventoryAndComponentExist_addsStockItem() {
        // Arrange
        var inventory = new TechnicianInventory(TECHNICIAN_ID);
        var component = componentWithUid(COMPONENT_ID);
        when(technicianInventoryRepository.findByTechnicianId(TECHNICIAN_ID)).thenReturn(Optional.of(inventory));
        when(componentRepository.findByComponentUid(COMPONENT_ID)).thenReturn(Optional.of(component));
        when(technicianInventoryRepository.save(inventory)).thenReturn(inventory);

        // Act
        var result = service.handle(new AddComponentStockCommand(TECHNICIAN_ID, COMPONENT_ID, 10, 3));

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getComponentStocks()).hasSize(1);
        var stock = result.get().getComponentStocks().get(0);
        assertThat(stock.getComponent()).isSameAs(component);
        assertThat(stock.getQuantityAvailable()).isEqualTo(10);
        assertThat(stock.getAlertThreshold()).isEqualTo(3);
    }

    @Test
    @DisplayName("AddStock: lanza EntityNotFoundException si el técnico no tiene inventario")
    void addStock_whenInventoryMissing_throwsEntityNotFoundException() {
        // Arrange
        when(technicianInventoryRepository.findByTechnicianId(TECHNICIAN_ID)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.handle(new AddComponentStockCommand(TECHNICIAN_ID, COMPONENT_ID, 10, 3)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("TechnicianInventory not found");
        verify(technicianInventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("AddStock: lanza EntityNotFoundException si el componente no existe")
    void addStock_whenComponentMissing_throwsEntityNotFoundException() {
        // Arrange
        when(technicianInventoryRepository.findByTechnicianId(TECHNICIAN_ID))
                .thenReturn(Optional.of(new TechnicianInventory(TECHNICIAN_ID)));
        when(componentRepository.findByComponentUid(COMPONENT_ID)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.handle(new AddComponentStockCommand(TECHNICIAN_ID, COMPONENT_ID, 10, 3)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Component not found");
        verify(technicianInventoryRepository, never()).save(any());
    }

    // ---------- UpdateComponentStockCommand ----------

    @Test
    @DisplayName("UpdateStock: actualiza cantidad y umbral del stock existente")
    void updateStock_whenStockExists_updatesQuantityAndThreshold() {
        // Arrange
        var inventory = inventoryWithStock(10, 3);
        when(technicianInventoryRepository.findByTechnicianId(TECHNICIAN_ID)).thenReturn(Optional.of(inventory));

        // Act
        var result = service.handle(new UpdateComponentStockCommand(TECHNICIAN_ID, COMPONENT_ID, 25, 5));

        // Assert
        assertThat(result).isPresent();
        var stock = result.get().getComponentStocks().get(0);
        assertThat(stock.getQuantityAvailable()).isEqualTo(25);
        assertThat(stock.getAlertThreshold()).isEqualTo(5);
        verify(technicianInventoryRepository).save(inventory);
    }

    @Test
    @DisplayName("UpdateStock: conserva el umbral actual si newAlertThreshold es null")
    void updateStock_whenThresholdIsNull_keepsCurrentThreshold() {
        // Arrange
        var inventory = inventoryWithStock(10, 3);
        when(technicianInventoryRepository.findByTechnicianId(TECHNICIAN_ID)).thenReturn(Optional.of(inventory));

        // Act
        var result = service.handle(new UpdateComponentStockCommand(TECHNICIAN_ID, COMPONENT_ID, 25, null));

        // Assert
        var stock = result.orElseThrow().getComponentStocks().get(0);
        assertThat(stock.getQuantityAvailable()).isEqualTo(25);
        assertThat(stock.getAlertThreshold()).isEqualTo(3);
    }

    @Test
    @DisplayName("UpdateStock: lanza EntityNotFoundException si el técnico no tiene inventario")
    void updateStock_whenInventoryMissing_throwsEntityNotFoundException() {
        // Arrange
        when(technicianInventoryRepository.findByTechnicianId(TECHNICIAN_ID)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.handle(new UpdateComponentStockCommand(TECHNICIAN_ID, COMPONENT_ID, 25, 5)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("TechnicianInventory not found");
    }

    @Test
    @DisplayName("UpdateStock: lanza EntityNotFoundException si el componente no está en el inventario")
    void updateStock_whenComponentNotInInventory_throwsEntityNotFoundException() {
        // Arrange
        when(technicianInventoryRepository.findByTechnicianId(TECHNICIAN_ID))
                .thenReturn(Optional.of(new TechnicianInventory(TECHNICIAN_ID)));

        // Act + Assert
        assertThatThrownBy(() -> service.handle(new UpdateComponentStockCommand(TECHNICIAN_ID, COMPONENT_ID, 25, 5)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Component not found in technician's inventory");
        verify(technicianInventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("UpdateStock: rechaza una cantidad negativa")
    void updateStock_whenQuantityIsNegative_throwsIllegalArgumentException() {
        // Arrange
        var inventory = inventoryWithStock(10, 3);
        when(technicianInventoryRepository.findByTechnicianId(TECHNICIAN_ID)).thenReturn(Optional.of(inventory));

        // Act + Assert
        assertThatThrownBy(() -> service.handle(new UpdateComponentStockCommand(TECHNICIAN_ID, COMPONENT_ID, -1, 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity cannot be negative.");
        verify(technicianInventoryRepository, never()).save(any());
    }

    // ---------- DeleteComponentStockCommand ----------

    @Test
    @DisplayName("DeleteStock: quita el componente del inventario y devuelve true")
    void deleteStock_whenStockExists_removesItAndReturnsTrue() {
        // Arrange
        var inventory = inventoryWithStock(10, 3);
        when(technicianInventoryRepository.findByTechnicianId(TECHNICIAN_ID)).thenReturn(Optional.of(inventory));

        // Act
        var result = service.handle(new DeleteComponentStockCommand(TECHNICIAN_ID, COMPONENT_ID));

        // Assert
        assertThat(result).isTrue();
        assertThat(inventory.getComponentStocks()).isEmpty();
        verify(technicianInventoryRepository).save(inventory);
    }

    @Test
    @DisplayName("DeleteStock: devuelve false si el componente no está en el inventario")
    void deleteStock_whenComponentNotInInventory_returnsFalse() {
        // Arrange
        var inventory = new TechnicianInventory(TECHNICIAN_ID);
        when(technicianInventoryRepository.findByTechnicianId(TECHNICIAN_ID)).thenReturn(Optional.of(inventory));

        // Act
        var result = service.handle(new DeleteComponentStockCommand(TECHNICIAN_ID, COMPONENT_ID));

        // Assert
        assertThat(result).isFalse();
        verify(technicianInventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("DeleteStock: devuelve false si el técnico no tiene inventario")
    void deleteStock_whenInventoryMissing_returnsFalse() {
        // Arrange
        when(technicianInventoryRepository.findByTechnicianId(TECHNICIAN_ID)).thenReturn(Optional.empty());

        // Act
        var result = service.handle(new DeleteComponentStockCommand(TECHNICIAN_ID, COMPONENT_ID));

        // Assert
        assertThat(result).isFalse();
        verify(technicianInventoryRepository, never()).save(any());
    }
}
