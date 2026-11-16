package com.coordinationsystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "allocations")
@Getter @Setter @NoArgsConstructor
public class Allocation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "allocation_code", nullable = false, unique = true, length = 50)
    private String allocationCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "request_id", nullable = false)
    private AssistanceRequest request;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "coordinator_id", nullable = false)
    private User coordinator;

    @Column(nullable = false, length = 30)
    private String status = "PENDING";

    @Column(name = "allocated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime allocatedAt;

    @Column(columnDefinition = "nvarchar(max)")
    private String note;
}
