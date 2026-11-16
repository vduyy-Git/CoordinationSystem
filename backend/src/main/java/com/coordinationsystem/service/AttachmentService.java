package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.Attachment;
import com.coordinationsystem.repository.AttachmentRepository;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AttachmentService {
    private final AttachmentRepository repository;

    public AttachmentService(AttachmentRepository repository) {
        this.repository = repository;
    }

    public List<Attachment> getAll() {
        return repository.findAll();
    }

    public Attachment getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public Attachment create(Attachment entity) {
        setEntityId(entity, null);
        return repository.save(entity);
    }

    public Attachment update(Long id, Attachment entity) {
        getById(id);
        setEntityId(entity, id);
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void setEntityId(Attachment entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
