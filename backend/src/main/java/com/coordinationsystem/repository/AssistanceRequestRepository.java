package com.coordinationsystem.repository;

import com.coordinationsystem.model.AssistanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssistanceRequestRepository extends JpaRepository<AssistanceRequest, Long> {
}
