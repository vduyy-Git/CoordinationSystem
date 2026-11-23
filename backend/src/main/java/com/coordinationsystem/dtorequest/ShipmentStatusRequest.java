package com.coordinationsystem.dtorequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShipmentStatusRequest(@NotBlank String status, @Size(max = 10000) String description,
                                    @Size(max = 500) String location) { }
