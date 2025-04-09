package com.project.admin.service;

import com.project.admin.entity.Notification;
import com.project.admin.entity.User;
import com.project.admin.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
