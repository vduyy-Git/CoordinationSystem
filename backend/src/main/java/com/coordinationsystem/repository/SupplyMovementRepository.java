package com.coordinationsystem.repository;

import com.coordinationsystem.model.SupplyMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplyMovementRepository extends JpaRepository<SupplyMovement, Long> {
}
