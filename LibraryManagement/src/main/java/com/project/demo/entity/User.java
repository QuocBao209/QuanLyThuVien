package com.project.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String cmt;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String name;

    private String phone;
    private String email;
    private String username;
    private String password;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String status;
    private String role;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int borrowCount;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int violationCount;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Borrow_Return> borrowReturns;
}
