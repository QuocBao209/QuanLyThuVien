package com.project.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "author")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long authorId;

    @Column(columnDefinition = "NVARCHAR(255)", nullable = false, unique = true)
    private String authorName;

    @EqualsAndHashCode.Exclude
    @ManyToMany(mappedBy = "authors")
    private List<Book> books = new ArrayList<>();

    public Author(String authorName) {
        this.authorName = authorName;
    }


    @Override
    public String toString() {
        return authorName;
    }
}