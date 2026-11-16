package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.CollectionPoint;
import com.coordinationsystem.repository.CollectionPointRepository;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CollectionPointService {
    private final CollectionPointRepository repository;

    public CollectionPointService(CollectionPointRepository repository) {
        this.repository = repository;
    }

    public List<CollectionPoint> getAll() {
        return repository.findAll();
    }

    public CollectionPoint getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public CollectionPoint create(CollectionPoint entity) {
        setEntityId(entity, null);
        return repository.save(entity);
    }

    public CollectionPoint update(Long id, CollectionPoint entity) {
        getById(id);
        setEntityId(entity, id);
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void setEntityId(CollectionPoint entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
