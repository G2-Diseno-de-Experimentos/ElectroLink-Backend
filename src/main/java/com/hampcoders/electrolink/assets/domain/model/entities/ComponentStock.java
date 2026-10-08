package com.hampcoders.electrolink.assets.domain.model.entities;

import com.hampcoders.electrolink.assets.domain.model.aggregates.Component;
import com.hampcoders.electrolink.assets.domain.model.aggregates.TechnicianInventory;
import com.hampcoders.electrolink.assets.domain.model.valueobjects.ComponentId;
import com.hampcoders.electrolink.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name="component_stocks")
@Getter
public class ComponentStock extends AuditableModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "component_stock_uid", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "technician_inventory_id")
    private TechnicianInventory technicianInventory;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "component_id", nullable = false)
    private Component component;

    private int quantityAvailable;

    private int alertThreshold;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    public ComponentStock() {

    }

    public ComponentStock(TechnicianInventory technicianInventory, Component component, int quantityAvailable, int alertThreshold, Date lastUpdated) {
        this.technicianInventory = technicianInventory;
        this.component = component;
        this.quantityAvailable = quantityAvailable;
        this.alertThreshold = alertThreshold;
        this.lastUpdated = lastUpdated;
    }

    public void updateQuantity(int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        this.quantityAvailable = newQuantity;
    }

    public void increaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Quantity to add must be greater than zero.");
        }
        this.quantityAvailable += amount;
        this.lastUpdated = new Date();
    }

    public void updateAlertThreshold(int newAlertThreshold) {
        if (newAlertThreshold < 0) {
            throw new IllegalArgumentException("Alert threshold cannot be negative.");
        }
        this.alertThreshold = newAlertThreshold;
    }

    public void updateLastUpdated(Date date) {
        this.lastUpdated = date;
    }
}
