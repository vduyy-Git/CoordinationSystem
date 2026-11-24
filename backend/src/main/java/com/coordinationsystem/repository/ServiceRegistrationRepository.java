package com.coordinationsystem.repository;

import com.coordinationsystem.model.ServiceRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceRegistrationRepository extends JpaRepository<ServiceRegistration, Long> {
    List<ServiceRegistration> findByUser_EmailIgnoreCaseOrderByCreatedAtDesc(String email);
}
