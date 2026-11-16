package com.coordinationsystem.repository;

import com.coordinationsystem.model.RequestItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestItemRepository extends JpaRepository<RequestItem, Long> {
}
