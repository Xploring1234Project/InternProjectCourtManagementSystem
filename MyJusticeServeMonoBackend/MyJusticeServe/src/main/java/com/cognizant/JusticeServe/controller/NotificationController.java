package com.cognizant.JusticeServe.controller;

import com.cognizant.JusticeServe.dto.request.NotificationRequest;
import com.cognizant.JusticeServe.dto.response.NotificationResponse;
import com.cognizant.JusticeServe.service.NotificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Import for SLF4J
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j // Lombok annotation for the 'log' object
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/dispatch")
    public ResponseEntity<NotificationResponse> sendNotification(@Valid @RequestBody NotificationRequest request) {
        log.info("Request to dispatch notification to User ID: {}. Type: {}",
                request.getUserId(), request.getCategory());

        NotificationResponse response = notificationService.dispatchNotification(request);

        log.info("Notification successfully dispatched. ID: {}", response.getNotificationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getUserNotificationHistory(@PathVariable Long userId) {
        log.info("Fetching notification history for User ID: {}", userId);
        return ResponseEntity.ok(notificationService.fetchUserNotifications(userId));
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadAlertCount(@PathVariable Long userId) {
        log.debug("Polling unread notification count for User ID: {}", userId);
        return ResponseEntity.ok(notificationService.fetchUnreadCountForUser(userId));
    }

    @PatchMapping("/{id}/mark-as-read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        log.info("Marking notification ID: {} as read", id);
        return ResponseEntity.ok(notificationService.updateNotificationStatusToRead(id));
    }

    @PatchMapping("/user/{userId}/clear-all")
    public ResponseEntity<Void> clearAllUnread(@PathVariable Long userId) {
        log.info("Request to clear all notifications for User ID: {}", userId);
        notificationService.markAllUserNotificationsAsRead(userId);
        log.debug("All notifications marked as read for User ID: {}", userId);
        return ResponseEntity.noContent().build();
    }
}