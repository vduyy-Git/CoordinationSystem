package com.coordinationsystem.repository;

import com.coordinationsystem.model.SocialService;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SocialServiceRepository extends JpaRepository<SocialService, Long> {
    List<SocialService> findByStatusIgnoreCaseOrderByNameAsc(String status);
}
