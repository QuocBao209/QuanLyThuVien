package com.project.demo.service;

import com.project.demo.entity.Notification;
import com.project.demo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public List<Notification> getNotificationsByUsername(String username) {
        return notificationRepository.findNotificationsByUsername(username);
    }


    public void transferData(List<Notification> notifications) {
        notificationRepository.saveAll(notifications);
    }


    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
}
