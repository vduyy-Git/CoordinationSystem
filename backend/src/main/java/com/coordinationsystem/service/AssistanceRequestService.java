package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.AssistanceRequest;
import com.coordinationsystem.repository.AssistanceRequestRepository;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AssistanceRequestService {
    private final AssistanceRequestRepository repository;

    public AssistanceRequestService(AssistanceRequestRepository repository) {
        this.repository = repository;
    }

    public List<AssistanceRequest> getAll() {
        return repository.findAll();
    }

    public AssistanceRequest getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public AssistanceRequest create(AssistanceRequest entity) {
        setEntityId(entity, null);
        return repository.save(entity);
    }

    public AssistanceRequest update(Long id, AssistanceRequest entity) {
        getById(id);
        setEntityId(entity, id);
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void setEntityId(AssistanceRequest entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
