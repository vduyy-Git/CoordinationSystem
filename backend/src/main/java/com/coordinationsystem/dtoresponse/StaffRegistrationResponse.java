package com.coordinationsystem.dtoresponse;

import java.time.LocalDateTime;

public record StaffRegistrationResponse(Long id, String fullName, String email, String phone, String department,
                                        String status, LocalDateTime createdAt, LocalDateTime reviewedAt) { }
