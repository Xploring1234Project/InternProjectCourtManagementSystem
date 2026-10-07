package com.justiceserve.notificationservice.service;

import com.justiceserve.notificationservice.dto.NotificationRequest;
import com.justiceserve.notificationservice.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {
    NotificationResponse sendNotification(NotificationRequest request);
    List<NotificationResponse> fetchUserNotifications(Long userId);
    NotificationResponse updateNotificationStatusToRead(Long id);
    void markAllUserNotificationsAsRead(Long userId);
    long fetchUnreadCountForUser(Long userId);
}
