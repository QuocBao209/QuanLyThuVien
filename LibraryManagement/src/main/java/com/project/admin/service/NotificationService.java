package com.project.admin.service;

import com.project.admin.entity.Notification;
import com.project.admin.repository.NotificationRepository;
import org.springframework.stereotype.Service;


@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void save(Notification notification) {
        notificationRepository.save(notification);
    }

}
