package com.coordinationsystem.dtorequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateShipmentRequest(@NotNull Long allocationId, Long partnerId,
                                    @NotBlank @Size(max = 500) String pickupAddress,
                                    @NotBlank @Size(max = 500) String deliveryAddress,
                                    @Size(max = 10000) String note) { }
