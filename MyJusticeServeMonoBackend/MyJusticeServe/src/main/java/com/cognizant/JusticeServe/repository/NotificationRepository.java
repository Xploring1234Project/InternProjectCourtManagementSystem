package com.cognizant.JusticeServe.repository;

import com.cognizant.JusticeServe.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserUserIdOrderByCreatedDateDesc(Long userId);
    List<Notification> findByUserUserIdAndStatus(Long userId, Notification.NotificationStatus status);
    long countByUserUserIdAndStatus(Long userId, Notification.NotificationStatus status);
}

