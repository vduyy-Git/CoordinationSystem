package com.coordinationsystem.repository;

import com.coordinationsystem.model.DonationItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationItemRepository extends JpaRepository<DonationItem, Long> {
}
