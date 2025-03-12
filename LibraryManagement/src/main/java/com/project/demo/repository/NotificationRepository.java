package com.project.demo.repository;

import com.project.demo.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    @Query(value = "SELECT * FROM notifications WHERE user_id = (SELECT user_id FROM users WHERE username = ?1)", nativeQuery = true)
    List<Notification> findNotificationsByUsername(String username);

}
