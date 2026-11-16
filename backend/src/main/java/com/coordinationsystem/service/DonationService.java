package com.coordinationsystem.service;

import com.coordinationsystem.exception.NotFoundException;
import com.coordinationsystem.model.Donation;
import com.coordinationsystem.repository.DonationRepository;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DonationService {
    private final DonationRepository repository;

    public DonationService(DonationRepository repository) {
        this.repository = repository;
    }

    public List<Donation> getAll() {
        return repository.findAll();
    }

    public Donation getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dữ liệu với id: " + id));
    }

    public Donation create(Donation entity) {
        setEntityId(entity, null);
        return repository.save(entity);
    }

    public Donation update(Long id, Donation entity) {
        getById(id);
        setEntityId(entity, id);
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private void setEntityId(Donation entity, Long id) {
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
    }
}
