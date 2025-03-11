package com.project.demo.repository;

import com.project.demo.entity.Notification;
import com.project.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<com.project.demo.entity.Notification, Integer> {
    List<Notification> findByUserOrderByCreatedAtDesc(Optional<User> user);
}
