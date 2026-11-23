package com.coordinationsystem.repository;

import com.coordinationsystem.model.AssistanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface AssistanceRequestRepository extends JpaRepository<AssistanceRequest, Long> {
    List<AssistanceRequest> findByRecipient_EmailIgnoreCaseOrderByCreatedAtDesc(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from AssistanceRequest r where r.id = :id")
    Optional<AssistanceRequest> findLockedById(@Param("id") Long id);
}
