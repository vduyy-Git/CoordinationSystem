package com.coordinationsystem.dtorequest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank @Size(max = 150) String fullName,
        @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @Size(max = 20) String phone,
        @Pattern(regexp = "(?:[0-9]{12})?", message = "CCCD phải gồm đúng 12 chữ số") @Size(max = 20) String citizenId,
        LocalDate birthDate,
        @Size(max = 500) String address,
        @Size(max = 100) String ward,
        @Size(max = 100) String beneficiaryGroup,
        @Size(max = 10000) String situationDescription) {
}
