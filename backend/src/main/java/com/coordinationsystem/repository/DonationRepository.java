package com.coordinationsystem.repository;

import com.coordinationsystem.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByDonor_EmailIgnoreCaseOrderByCreatedAtDesc(String email);
    long countByStatusIgnoreCase(String status);
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Donation d where d.id = :id")
    Optional<Donation> findLockedById(@Param("id") Long id);
}
