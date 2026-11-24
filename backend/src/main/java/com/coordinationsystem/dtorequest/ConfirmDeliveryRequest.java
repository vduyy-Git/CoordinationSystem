package com.coordinationsystem.dtorequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ConfirmDeliveryRequest(@NotNull Long shipmentId, @NotBlank @Size(max = 150) String receivedBy,
                                     @Size(max = 100) String confirmationCode,
                                     @Size(max = 1000) String imageUrl,
                                     @Size(max = 10000) String note,
                                     @Size(max = 30) String deliveryCondition,
                                     @Min(1) @Max(5) Integer rating) { }
