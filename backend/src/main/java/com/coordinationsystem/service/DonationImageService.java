package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.DonationImage;
import com.coordinationsystem.repository.DonationImageRepository;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DonationImageService {
    private final DonationImageRepository repository;

    public DonationImageService(DonationImageRepository repository) {
        this.repository = repository;
    }

    public List<DonationImage> getAll() {
        return repository.findAll();
    }

    public DonationImage getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public DonationImage create(DonationImage entity) {
        setEntityId(entity, null);
        return repository.save(entity);
    }

    public DonationImage update(Long id, DonationImage entity) {
        getById(id);
        setEntityId(entity, id);
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void setEntityId(DonationImage entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
