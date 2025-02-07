package com.project.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Book")
@Data
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    private String bookName;
    private int amount;
    private int publishYear;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private Author author;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    // Optional: Override toString() for better output representation
    @Override
    public String toString() {
        return bookName + " by " + (author != null ? author.getAuthorName() : "Unknown Author");
    }
}
