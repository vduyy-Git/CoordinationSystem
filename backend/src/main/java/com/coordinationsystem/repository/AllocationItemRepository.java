package com.coordinationsystem.repository;

import com.coordinationsystem.model.AllocationItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllocationItemRepository extends JpaRepository<AllocationItem, Long> {
}
