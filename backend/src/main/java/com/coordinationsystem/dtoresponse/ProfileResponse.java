package com.coordinationsystem.dtoresponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ProfileResponse(Long id, String fullName, String email, String phone, String citizenId,
                              LocalDate birthDate, String address, String ward, String beneficiaryGroup,
                              String situationDescription, String avatar, String status, List<String> roles,
                              LocalDateTime createdAt) { }
