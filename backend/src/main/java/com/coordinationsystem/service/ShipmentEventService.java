package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.ShipmentEvent;
import com.coordinationsystem.repository.ShipmentEventRepository;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ShipmentEventService {
    private final ShipmentEventRepository repository;

    public ShipmentEventService(ShipmentEventRepository repository) {
        this.repository = repository;
    }

    public List<ShipmentEvent> getAll() {
        return repository.findAll();
    }

    public ShipmentEvent getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public ShipmentEvent create(ShipmentEvent entity) {
        setEntityId(entity, null);
        return repository.save(entity);
    }

    public ShipmentEvent update(Long id, ShipmentEvent entity) {
        getById(id);
        setEntityId(entity, id);
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void setEntityId(ShipmentEvent entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
