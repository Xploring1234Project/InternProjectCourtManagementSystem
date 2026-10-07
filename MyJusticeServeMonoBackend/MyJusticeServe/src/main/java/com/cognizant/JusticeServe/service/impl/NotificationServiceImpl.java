package com.cognizant.JusticeServe.service.impl;

import com.cognizant.JusticeServe.dto.request.NotificationRequest;
import com.cognizant.JusticeServe.dto.response.NotificationResponse;
import com.cognizant.JusticeServe.entity.Notification;
import com.cognizant.JusticeServe.entity.User;
import com.cognizant.JusticeServe.exception.ResourceNotFoundException;
import com.cognizant.JusticeServe.repository.NotificationRepository;
import com.cognizant.JusticeServe.repository.UserRepository;
import com.cognizant.JusticeServe.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepo;
    private final UserRepository userRepo;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public NotificationResponse dispatchNotification(NotificationRequest req) {
        User recipient = userRepo.findById(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found: " + req.getUserId()));

        Notification notification = modelMapper.map(req, Notification.class);
        notification.setUser(recipient); // Set the managed User entity
        notification.setStatus(Notification.NotificationStatus.UNREAD);

        return modelMapper.map(notificationRepo.save(notification), NotificationResponse.class);
    }

    @Override
    public List<NotificationResponse> fetchUserNotifications(Long userId) {
        return notificationRepo.findByUserUserIdOrderByCreatedDateDesc(userId)
                .stream()
                .map(n -> modelMapper.map(n, NotificationResponse.class))
                .toList();
    }

    @Override
    @Transactional
    public NotificationResponse updateNotificationStatusToRead(Long id) {
        Notification notification = notificationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));

        notification.setStatus(Notification.NotificationStatus.READ);
        return modelMapper.map(notificationRepo.save(notification), NotificationResponse.class);
    }

    @Override
    public long fetchUnreadCountForUser(Long userId) {
        return notificationRepo.countByUserUserIdAndStatus(userId, Notification.NotificationStatus.UNREAD);
    }

    @Override
    @Transactional
    public void markAllUserNotificationsAsRead(Long userId) {
        List<Notification> unreadList = notificationRepo.findByUserUserIdAndStatus(userId, Notification.NotificationStatus.UNREAD);
        if (!unreadList.isEmpty()) {
            unreadList.forEach(n -> n.setStatus(Notification.NotificationStatus.READ));
            notificationRepo.saveAll(unreadList);
        }
    }
}