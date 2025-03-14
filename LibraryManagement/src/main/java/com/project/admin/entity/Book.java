package com.project.admin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "book")
@Data
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    @Column(columnDefinition = "NVARCHAR(255)", nullable = false, unique = false)
    private String bookName;

    private int amount;
    private int publishYear;

    @Column(columnDefinition = "NVARCHAR(500)")
    private String bookImage;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @EqualsAndHashCode.Exclude
    @ManyToMany
    @JoinTable(
            name = "book_author",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private List<Author> authors = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<Borrow_Return> borrowReturns = new ArrayList<>();

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int borrowCount;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int isDamaged;

    @Override
    public String toString() {
        return bookName + " by " + (authors != null ? authors.toString() : "Unknown Author");
    }
}
