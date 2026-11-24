package com.coordinationsystem.repository;

import com.coordinationsystem.model.StaffRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StaffRegistrationRepository extends JpaRepository<StaffRegistration, Long> {
    boolean existsByEmailIgnoreCase(String email);
    List<StaffRegistration> findAllByOrderByCreatedAtDesc();
}
