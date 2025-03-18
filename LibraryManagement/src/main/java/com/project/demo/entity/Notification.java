package com.project.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;



    @Column(columnDefinition = "NVARCHAR(255)")
    private String message;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String type;

    private LocalDateTime createdAt;
    private boolean isRead = false;
}
