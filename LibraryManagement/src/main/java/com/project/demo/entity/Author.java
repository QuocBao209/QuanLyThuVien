package com.project.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Author")
@Data
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long authorId;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String authorName;

    @Override
    public String toString() {
        return authorName;
    }
}
