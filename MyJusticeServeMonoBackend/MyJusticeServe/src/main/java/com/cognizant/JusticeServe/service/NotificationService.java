package com.cognizant.JusticeServe.service;

import com.cognizant.JusticeServe.dto.request.NotificationRequest;
import com.cognizant.JusticeServe.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {
    NotificationResponse dispatchNotification(NotificationRequest request);
    List<NotificationResponse> fetchUserNotifications(Long userId);
    NotificationResponse updateNotificationStatusToRead(Long id);
    void markAllUserNotificationsAsRead(Long userId);
    long fetchUnreadCountForUser(Long userId);
}
