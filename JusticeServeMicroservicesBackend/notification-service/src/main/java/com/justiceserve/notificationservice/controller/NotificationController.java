package com.justiceserve.notificationservice.controller;

import com.justiceserve.notificationservice.dto.NotificationRequest;
import com.justiceserve.notificationservice.dto.NotificationResponse;
import com.justiceserve.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;

    @PostMapping("/internal")
    public ResponseEntity<NotificationResponse> sendInternal(@RequestBody NotificationRequest req) {
        return ResponseEntity.ok(service.sendNotification(req));
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> sendNotification(@Valid @RequestBody NotificationRequest req) {
        return ResponseEntity.ok(service.sendNotification(req));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(service.fetchUserNotifications(userId));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(service.updateNotificationStatusToRead(id));
    }

    @PatchMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllRead(@PathVariable Long userId) {
        service.markAllUserNotificationsAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> countUnread(@PathVariable Long userId) {
        return ResponseEntity.ok(service.fetchUnreadCountForUser(userId));
    }
}
