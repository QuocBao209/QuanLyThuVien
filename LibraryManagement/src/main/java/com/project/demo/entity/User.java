package com.project.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "Users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String cmt;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String name;

    private String phone;
    private String email;
    private String username;
    private String password;
    private String status;
    private String role;
}
