package com.justiceserve.notificationservice.dto;

import com.justiceserve.notificationservice.entity.Notification;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long notificationId;
    private Long userId;
    private Long entityId;
    private String message;
    private Notification.NotificationCategory category;
    private Notification.NotificationStatus status;
    private LocalDateTime createdDate;
}