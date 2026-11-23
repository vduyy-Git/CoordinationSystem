package com.coordinationsystem.repository;

import com.coordinationsystem.model.RequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;

public interface RequestItemRepository extends JpaRepository<RequestItem, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RequestItem r where r.id = :id")
    Optional<RequestItem> findLockedById(@Param("id") Long id);
    List<RequestItem> findByRequest_Id(Long requestId);
}
