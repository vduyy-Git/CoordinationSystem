package com.coordinationsystem.repository;

import com.coordinationsystem.model.DeliveryConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeliveryConfirmationRepository extends JpaRepository<DeliveryConfirmation, Long> {
    List<DeliveryConfirmation> findByRecipient_EmailIgnoreCaseOrderByConfirmedAtDesc(String email);
}
