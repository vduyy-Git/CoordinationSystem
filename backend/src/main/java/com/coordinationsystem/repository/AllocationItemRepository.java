package com.coordinationsystem.repository;

import com.coordinationsystem.model.AllocationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;

public interface AllocationItemRepository extends JpaRepository<AllocationItem, Long> {
    List<AllocationItem> findByAllocation_Id(Long allocationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AllocationItem a where a.id = :id")
    java.util.Optional<AllocationItem> findLockedById(@Param("id") Long id);
    @Query("select coalesce(sum(a.quantity), 0) from AllocationItem a where a.requestItem.id = :id")
    BigDecimal sumAllocatedForRequestItem(@Param("id") Long requestItemId);

    @Query("select coalesce(sum(a.quantity), 0) from AllocationItem a where a.donationItem.id = :id")
    BigDecimal sumAllocatedFromDonationItem(@Param("id") Long donationItemId);
}
