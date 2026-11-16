package com.coordinationsystem.repository;

import com.coordinationsystem.model.ShipmentItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentItemRepository extends JpaRepository<ShipmentItem, Long> {
}
