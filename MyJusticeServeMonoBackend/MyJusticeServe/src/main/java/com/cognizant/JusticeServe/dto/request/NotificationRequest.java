package com.cognizant.JusticeServe.dto.request;

import com.cognizant.JusticeServe.entity.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationRequest {
    @NotNull(message = "Target User ID is required")
    private Long userId;
    @NotNull(message = "Entity ID is required")
    private Long entityId;
    @NotBlank(message = "Notification message cannot be empty")
    private String message;
    @NotNull(message = "Category is required")
    private Notification.NotificationCategory category;
}