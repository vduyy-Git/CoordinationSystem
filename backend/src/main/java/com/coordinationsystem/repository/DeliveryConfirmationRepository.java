package com.coordinationsystem.repository;

import com.coordinationsystem.model.DeliveryConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryConfirmationRepository extends JpaRepository<DeliveryConfirmation, Long> {
}
