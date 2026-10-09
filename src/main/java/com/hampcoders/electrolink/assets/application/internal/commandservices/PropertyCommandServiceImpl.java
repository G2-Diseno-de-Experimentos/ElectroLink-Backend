package com.hampcoders.electrolink.assets.application.internal.commandservices;

import com.hampcoders.electrolink.assets.domain.model.aggregates.Property;
import com.hampcoders.electrolink.assets.domain.model.commands.CreatePropertyCommand;
import com.hampcoders.electrolink.assets.domain.model.commands.DeletePropertyCommand;
import com.hampcoders.electrolink.assets.domain.model.commands.UpdatePropertyCommand;
import com.hampcoders.electrolink.assets.domain.services.PropertyCommandService;
import com.hampcoders.electrolink.assets.infrastructure.persistence.jpa.repositories.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class PropertyCommandServiceImpl implements PropertyCommandService {

    private final PropertyRepository propertyRepository;

    public PropertyCommandServiceImpl(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Override
    public UUID handle(CreatePropertyCommand command) {
        var property = new Property(command);
        propertyRepository.save(property);
        return property.getId();
    }

    @Override
    public Optional<Property> handle(UpdatePropertyCommand command) {
        return propertyRepository.findById(command.propertyId()).map(property -> {
            property.update(command);
            return propertyRepository.save(property);
        });
    }

    @Override
    public Boolean handle(DeletePropertyCommand command) {
        return propertyRepository.findById(command.propertyId()).map(property -> {
            propertyRepository.delete(property);
            return true;
        }).orElse(false);
    }
}
