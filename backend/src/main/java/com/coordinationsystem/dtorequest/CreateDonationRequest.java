package com.coordinationsystem.dtorequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDonationRequest(@NotBlank @Size(max = 500) String pickupAddress,
                                    @NotBlank @Size(max = 30) String donationType,
                                    @Size(max = 10000) String note) { }
