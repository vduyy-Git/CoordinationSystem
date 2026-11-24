package com.coordinationsystem.dtoresponse;

import java.time.LocalDateTime;

public record NotificationResponse(String id, String type, String title, String message,
                                   LocalDateTime createdAt, boolean read) { }
