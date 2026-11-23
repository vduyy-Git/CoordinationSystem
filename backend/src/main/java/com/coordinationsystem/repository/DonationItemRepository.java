package com.coordinationsystem.repository;

import com.coordinationsystem.model.DonationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;

public interface DonationItemRepository extends JpaRepository<DonationItem, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DonationItem d where d.id = :id")
    Optional<DonationItem> findLockedById(@Param("id") Long id);
    List<DonationItem> findByDonation_Id(Long donationId);
}
