package com.coordinationsystem.dtorequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAssistanceRequest(@NotBlank @Size(max = 200) String title,
                                      @Size(max = 10000) String description,
                                      @NotBlank @Size(max = 500) String deliveryAddress,
                                      @NotBlank @Size(max = 30) String priority) { }
