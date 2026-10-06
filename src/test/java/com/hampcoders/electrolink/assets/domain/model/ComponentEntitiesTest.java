package com.hampcoders.electrolink.assets.domain.model;

import com.hampcoders.electrolink.assets.domain.model.aggregates.Component;
import com.hampcoders.electrolink.assets.domain.model.aggregates.TechnicianInventory;
import com.hampcoders.electrolink.assets.domain.model.commands.CreateComponentCommand;
import com.hampcoders.electrolink.assets.domain.model.commands.UpdateComponentStockCommand;
import com.hampcoders.electrolink.assets.domain.model.entities.ComponentStock;
import com.hampcoders.electrolink.assets.domain.model.valueobjects.ComponentId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ComponentEntitiesTest {

    private static Component componentWithUid(Long uid) {
        var component = new Component(new CreateComponentCommand(UUID.randomUUID(), "Breaker", "desc", 1L, true));
        ReflectionTestUtils.setField(component, "componentUid", uid);
        return component;
    }

    // ---------- Component ----------

    @Test
    @DisplayName("Component: se crea activo con los datos del comando")
    void component_whenCreatedFromCommand_isActiveWithCommandData() {
        // Arrange
        var command = new CreateComponentCommand(UUID.randomUUID(), "Breaker 20A", "Interruptor", 4L, false);

        // Act
        var component = new Component(command);

        // Assert
        assertThat(component.getName()).isEqualTo("Breaker 20A");
        assertThat(component.getDescription()).isEqualTo("Interruptor");
        assertThat(component.getComponentTypeId()).isEqualTo(4L);
        assertThat(component.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Component: updateInfo cambia nombre y descripción")
    void component_updateInfo_changesNameAndDescription() {
        // Arrange
        var component = componentWithUid(1L);

        // Act
        component.updateInfo("Nuevo", "Nueva descripción");

        // Assert
        assertThat(component.getName()).isEqualTo("Nuevo");
        assertThat(component.getDescription()).isEqualTo("Nueva descripción");
    }

    @Test
    @DisplayName("Component: deactivate lo marca como inactivo")
    void component_deactivate_marksComponentInactive() {
        // Arrange
        var component = componentWithUid(1L);

        // Act
        component.deactivate();

        // Assert
        assertThat(component.getIsActive()).isFalse();
    }

    // ---------- ComponentId ----------

    @Test
    @DisplayName("ComponentId: acepta un id positivo")
    void componentId_whenPositive_isCreated() {
        // Arrange + Act
        var id = new ComponentId(5L);

        // Assert
        assertThat(id.componentId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("ComponentId: rechaza null, cero y negativos")
    void componentId_whenNullZeroOrNegative_throwsIllegalArgumentException() {
        // Arrange + Act + Assert
        assertThatThrownBy(() -> new ComponentId(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ComponentId(0L)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ComponentId(-3L)).isInstanceOf(IllegalArgumentException.class);
    }

    // ---------- ComponentStock ----------

    @Test
    @DisplayName("ComponentStock: updateQuantity guarda la nueva cantidad")
    void componentStock_updateQuantity_whenValid_setsQuantity() {
        // Arrange
        var stock = new ComponentStock(new TechnicianInventory(1L), componentWithUid(1L), 10, 3, new Date());

        // Act
        stock.updateQuantity(0);

        // Assert
        assertThat(stock.getQuantityAvailable()).isZero();
    }

    @Test
    @DisplayName("ComponentStock: updateQuantity rechaza valores negativos")
    void componentStock_updateQuantity_whenNegative_throwsIllegalArgumentException() {
        // Arrange
        var stock = new ComponentStock(new TechnicianInventory(1L), componentWithUid(1L), 10, 3, new Date());

        // Act + Assert
        assertThatThrownBy(() -> stock.updateQuantity(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity cannot be negative.");
        assertThat(stock.getQuantityAvailable()).isEqualTo(10);
    }

    @Test
    @DisplayName("ComponentStock: updateAlertThreshold guarda el nuevo umbral")
    void componentStock_updateAlertThreshold_whenValid_setsThreshold() {
        // Arrange
        var stock = new ComponentStock(new TechnicianInventory(1L), componentWithUid(1L), 10, 3, new Date());

        // Act
        stock.updateAlertThreshold(7);

        // Assert
        assertThat(stock.getAlertThreshold()).isEqualTo(7);
    }

    @Test
    @DisplayName("ComponentStock: updateAlertThreshold rechaza valores negativos")
    void componentStock_updateAlertThreshold_whenNegative_throwsIllegalArgumentException() {
        // Arrange
        var stock = new ComponentStock(new TechnicianInventory(1L), componentWithUid(1L), 10, 3, new Date());

        // Act + Assert
        assertThatThrownBy(() -> stock.updateAlertThreshold(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Alert threshold cannot be negative.");
        assertThat(stock.getAlertThreshold()).isEqualTo(3);
    }

    // ---------- TechnicianInventory ----------

    @Test
    @DisplayName("TechnicianInventory: addToStock agrega un item con cantidad y umbral")
    void technicianInventory_addToStock_addsStockItem() {
        // Arrange
        var inventory = new TechnicianInventory(1L);
        var component = componentWithUid(20L);

        // Act
        inventory.addToStock(component, 10, 3);

        // Assert
        assertThat(inventory.getComponentStocks()).hasSize(1);
        var stock = inventory.getComponentStocks().get(0);
        assertThat(stock.getComponent()).isSameAs(component);
        assertThat(stock.getQuantityAvailable()).isEqualTo(10);
        assertThat(stock.getAlertThreshold()).isEqualTo(3);
        assertThat(stock.getTechnicianInventory()).isSameAs(inventory);
    }

    @Test
    @DisplayName("TechnicianInventory: updateStockItem actualiza el stock y devuelve true")
    void technicianInventory_updateStockItem_whenPresent_updatesAndReturnsTrue() {
        // Arrange
        var inventory = new TechnicianInventory(1L);
        inventory.addToStock(componentWithUid(20L), 10, 3);
        var command = new UpdateComponentStockCommand(1L, 20L, 25, 5);

        // Act
        var updated = inventory.updateStockItem(command);

        // Assert
        assertThat(updated).isTrue();
        var stock = inventory.getComponentStocks().get(0);
        assertThat(stock.getQuantityAvailable()).isEqualTo(25);
        assertThat(stock.getAlertThreshold()).isEqualTo(5);
    }

    @Test
    @DisplayName("TechnicianInventory: updateStockItem devuelve false si el componente no está")
    void technicianInventory_updateStockItem_whenAbsent_returnsFalse() {
        // Arrange
        var inventory = new TechnicianInventory(1L);

        // Act
        var updated = inventory.updateStockItem(new UpdateComponentStockCommand(1L, 99L, 25, 5));

        // Assert
        assertThat(updated).isFalse();
    }

    @Test
    @DisplayName("TechnicianInventory: updateStockItem conserva el umbral si es null")
    void technicianInventory_updateStockItem_whenThresholdNull_keepsThreshold() {
        // Arrange
        var inventory = new TechnicianInventory(1L);
        inventory.addToStock(componentWithUid(20L), 10, 3);

        // Act
        inventory.updateStockItem(new UpdateComponentStockCommand(1L, 20L, 25, null));

        // Assert
        assertThat(inventory.getComponentStocks().get(0).getAlertThreshold()).isEqualTo(3);
    }

    @Test
    @DisplayName("TechnicianInventory: removeStockItem quita el componente indicado")
    void technicianInventory_removeStockItem_whenPresent_returnsTrueAndRemoves() {
        // Arrange
        var inventory = new TechnicianInventory(1L);
        inventory.addToStock(componentWithUid(20L), 10, 3);

        // Act
        var removed = inventory.removeStockItem(20L);

        // Assert
        assertThat(removed).isTrue();
        assertThat(inventory.getComponentStocks()).isEmpty();
    }

    @Test
    @DisplayName("TechnicianInventory: removeStockItem devuelve false si el componente no está")
    void technicianInventory_removeStockItem_whenAbsent_returnsFalse() {
        // Arrange
        var inventory = new TechnicianInventory(1L);
        inventory.addToStock(componentWithUid(20L), 10, 3);

        // Act
        var removed = inventory.removeStockItem(99L);

        // Assert
        assertThat(removed).isFalse();
        assertThat(inventory.getComponentStocks()).hasSize(1);
    }
}
