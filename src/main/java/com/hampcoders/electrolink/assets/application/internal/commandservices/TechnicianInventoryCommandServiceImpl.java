package com.hampcoders.electrolink.assets.application.internal.commandservices;

import com.hampcoders.electrolink.assets.domain.model.aggregates.TechnicianInventory;
import com.hampcoders.electrolink.assets.domain.model.commands.AddComponentStockCommand;
import com.hampcoders.electrolink.assets.domain.model.commands.CreateTechnicianInventoryCommand;
import com.hampcoders.electrolink.assets.domain.model.commands.DeleteComponentStockCommand;
import com.hampcoders.electrolink.assets.domain.model.commands.UpdateComponentStockCommand;
import com.hampcoders.electrolink.assets.domain.model.entities.ComponentStock;
import com.hampcoders.electrolink.assets.domain.services.TechnicianInventoryCommandService;
import com.hampcoders.electrolink.assets.infrastructure.persistence.jpa.repositories.ComponentRepository;
import com.hampcoders.electrolink.assets.infrastructure.persistence.jpa.repositories.TechnicianInventoryRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class TechnicianInventoryCommandServiceImpl implements TechnicianInventoryCommandService {

    private final TechnicianInventoryRepository technicianInventoryRepository;
    private final ComponentRepository componentRepository;

    public TechnicianInventoryCommandServiceImpl(TechnicianInventoryRepository technicianInventoryRepository, ComponentRepository componentRepository) {
        this.technicianInventoryRepository = technicianInventoryRepository;
        this.componentRepository = componentRepository;
    }

    @Override
    public UUID handle(CreateTechnicianInventoryCommand command) {
        if (technicianInventoryRepository.existsByTechnicianId(command.technicianId().technicianId())) {
            throw new IllegalStateException("Technician inventory already exists for this technician ID");
        }
        var inventory = new TechnicianInventory(command);
        technicianInventoryRepository.save(inventory);
        return inventory.getId();
    }

    @Override
    public Optional<TechnicianInventory> handle(AddComponentStockCommand command) {
        var inventory = technicianInventoryRepository.findByTechnicianId(command.technicianId())
                .orElseThrow(() -> new EntityNotFoundException("TechnicianInventory not found for technician ID: " + command.technicianId()));

        var component = componentRepository.findByComponentUid(command.componentId())
                .orElseThrow(() -> new EntityNotFoundException("Component not found with UID: " + command.componentId()));

        inventory.addToStock(component, command.quantity(), command.alertThreshold());
        var savedInventory = technicianInventoryRepository.save(inventory);
        return Optional.of(savedInventory);
    }

    @Override
    @Transactional
    public Optional<TechnicianInventory> handle(UpdateComponentStockCommand command) {
        Long technicianId = command.technicianId();

        TechnicianInventory inventory = technicianInventoryRepository.findByTechnicianId(technicianId)
                .orElseThrow(() -> new EntityNotFoundException("TechnicianInventory not found for technician ID: " + technicianId));

        Optional<ComponentStock> stockOpt = inventory.getComponentStocks().stream()
                .filter(stock -> stock.getComponent().getComponentUid().equals(command.componentId()))
                .findFirst();

        if (stockOpt.isEmpty()) {
            throw new EntityNotFoundException("Component not found in technician's inventory: " + command.componentId());
        }

        ComponentStock stock = stockOpt.get();
        stock.updateQuantity(command.newQuantity());
        if (command.newAlertThreshold() != null) {
            stock.updateAlertThreshold(command.newAlertThreshold());
        }

        technicianInventoryRepository.save(inventory);
        return Optional.of(inventory);
    }

    @Override
    public Boolean handle(DeleteComponentStockCommand command) {
        Long technicianId = command.technicianId();

        return technicianInventoryRepository.findByTechnicianId(technicianId)
                .map(inventory -> {
                    boolean removed = inventory.removeStockItem(command.componentId());
                    if (removed) {
                        technicianInventoryRepository.save(inventory);
                    }
                    return removed;
                })
                .orElse(false);
    }
}