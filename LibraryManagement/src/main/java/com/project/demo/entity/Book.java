package com.project.demo.entity;

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

    public void addAuthor(Author author) {
        authors.add(author);
        author.getBooks().add(this);
    }


    public void removeAuthor(Author author) {
        authors.remove(author);
        author.getBooks().remove(this);
    }

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Override
    public String toString() {
        return bookName + " by " + (authors != null ? authors.toString() : "Unknown Author");
    }
}