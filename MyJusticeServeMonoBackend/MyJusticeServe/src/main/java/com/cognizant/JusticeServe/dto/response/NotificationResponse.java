package com.cognizant.JusticeServe.dto.response;

import com.cognizant.JusticeServe.entity.Notification;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long notificationId;
    private Long userId; // Mapped from User.userId
    private Long entityId;
    private String message;
    private Notification.NotificationCategory category;
    private Notification.NotificationStatus status;
    private LocalDateTime createdDate;
}
