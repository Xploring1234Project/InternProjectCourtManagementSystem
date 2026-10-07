package com.justiceserve.notificationservice.service.impl;

import com.justiceserve.notificationservice.dto.NotificationRequest;
import com.justiceserve.notificationservice.dto.NotificationResponse;
import com.justiceserve.notificationservice.entity.Notification;
import com.justiceserve.notificationservice.exception.ResourceNotFoundException;
import com.justiceserve.notificationservice.repository.NotificationRepository;
import com.justiceserve.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final ModelMapper modelMapper;
    private final NotificationRepository notificationRepo;

    @Override
    public NotificationResponse sendNotification(NotificationRequest request) {
        Notification notification = modelMapper.map(request, Notification.class);
        notification.setStatus(Notification.NotificationStatus.UNREAD);
        Notification savedNotification = notificationRepo.save(notification);
        return modelMapper.map(savedNotification, NotificationResponse.class);
    }

    @Override
    public List<NotificationResponse> fetchUserNotifications(Long userId) {
        return notificationRepo.findByUserIdOrderByCreatedDateDesc(userId)
                .stream()
                .map(notification -> modelMapper.map(notification, NotificationResponse.class))
                .toList();
    }

    @Override
    public NotificationResponse updateNotificationStatusToRead(Long id) {
        Notification notification = notificationRepo.findById(id).orElseThrow(()-> new ResourceNotFoundException("Notification not found: "+ id));
        notification.setStatus(Notification.NotificationStatus.READ);
        notificationRepo.save(notification);
        return modelMapper.map(notification, NotificationResponse.class);
    }

    @Override
    public void markAllUserNotificationsAsRead(Long userId) {
        var list = notificationRepo.findByUserIdAndStatus(userId, Notification.NotificationStatus.UNREAD);
        list.forEach(n -> n.setStatus(Notification.NotificationStatus.READ));
        notificationRepo.saveAll(list);
    }

    @Override
    public long fetchUnreadCountForUser(Long userId) {
        return  notificationRepo.countByUserIdAndStatus(userId, Notification.NotificationStatus.UNREAD);
    }
}
