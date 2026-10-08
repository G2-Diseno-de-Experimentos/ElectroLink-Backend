package com.hampcoders.electrolink.assets.domain.model.valueobjects;


import com.hampcoders.electrolink.assets.domain.model.aggregates.Component;
import com.hampcoders.electrolink.assets.domain.model.aggregates.TechnicianInventory;
import com.hampcoders.electrolink.assets.domain.model.entities.ComponentStock;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ToString
@Embeddable
@Getter
public class InventoryStockList {

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "technicianInventory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComponentStock> items ;


    public InventoryStockList() {
        this.items  = new ArrayList<>();
    }

    public void addItem(TechnicianInventory inventory, Component component, int quantity, int threshold) {
        for (ComponentStock item : this.items) {
            if (item.getComponent() != null && item.getComponent().getComponentUid().equals(component.getComponentUid())) {
                item.increaseQuantity(quantity);
                item.updateAlertThreshold(threshold);
                return;
            }
        }
        this.items.add(new ComponentStock(inventory, component, quantity, threshold, new Date()));
    }

}
