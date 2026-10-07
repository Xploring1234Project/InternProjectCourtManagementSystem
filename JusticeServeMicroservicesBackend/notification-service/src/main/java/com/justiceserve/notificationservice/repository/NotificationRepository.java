package com.justiceserve.notificationservice.repository;

import com.justiceserve.notificationservice.dto.NotificationRequest;
import com.justiceserve.notificationservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedDateDesc(Long userId);
    List<Notification> findByUserIdAndStatus(Long userId, Notification.NotificationStatus status);
    long countByUserIdAndStatus(Long userId, Notification.NotificationStatus status);
}
