package com.coordinationsystem.repository;

import com.coordinationsystem.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    @Query("select distinct s from Shipment s join ShipmentItem si on si.shipment = s join si.allocationItem ai join ai.requestItem ri join ri.request r where lower(r.recipient.email) = lower(:email) order by s.createdAt desc")
    List<Shipment> findForRecipient(@Param("email") String email);
    long countByStatusIgnoreCase(String status);
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Shipment s where s.id = :id")
    Optional<Shipment> findLockedById(@Param("id") Long id);
}
