package com.project.demo.service;

import com.project.demo.entity.Notification;
import com.project.demo.entity.User;
import com.project.demo.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getNotificationsByUser(Optional<User> user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }
}
