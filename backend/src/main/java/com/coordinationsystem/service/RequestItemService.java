package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.RequestItem;
import com.coordinationsystem.repository.RequestItemRepository;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RequestItemService {
    private final RequestItemRepository repository;

    public RequestItemService(RequestItemRepository repository) {
        this.repository = repository;
    }

    public List<RequestItem> getAll() {
        return repository.findAll();
    }

    public RequestItem getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public RequestItem create(RequestItem entity) {
        setEntityId(entity, null);
        return repository.save(entity);
    }

    public RequestItem update(Long id, RequestItem entity) {
        getById(id);
        setEntityId(entity, id);
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void setEntityId(RequestItem entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
