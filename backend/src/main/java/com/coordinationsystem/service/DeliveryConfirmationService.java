package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.DeliveryConfirmation;
import com.coordinationsystem.repository.DeliveryConfirmationRepository;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DeliveryConfirmationService {
    private final DeliveryConfirmationRepository repository;

    public DeliveryConfirmationService(DeliveryConfirmationRepository repository) {
        this.repository = repository;
    }

    public List<DeliveryConfirmation> getAll() {
        return repository.findAll();
    }

    public DeliveryConfirmation getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public DeliveryConfirmation create(DeliveryConfirmation entity) {
        setEntityId(entity, null);
        return repository.save(entity);
    }

    public DeliveryConfirmation update(Long id, DeliveryConfirmation entity) {
        getById(id);
        setEntityId(entity, id);
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void setEntityId(DeliveryConfirmation entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
