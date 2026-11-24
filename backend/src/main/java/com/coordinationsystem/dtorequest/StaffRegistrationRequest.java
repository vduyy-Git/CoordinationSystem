package com.coordinationsystem.dtorequest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonAlias;

public record StaffRegistrationRequest(@NotBlank @Size(max = 150) String fullName,
                                       @NotBlank @Email @Size(max = 150) String email,
                                       @Size(max = 20) String phone,
                                       @NotBlank @Size(max = 150) String department,
                                       @JsonAlias("authCode") @NotBlank @Pattern(regexp = "[0-9]{6}") String authorizationPin,
                                       @NotBlank @Size(min = 8, max = 72) String password) { }
