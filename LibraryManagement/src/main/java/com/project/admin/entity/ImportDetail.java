package com.project.admin.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "import_details")
@Data
public class ImportDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long detailId;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private ImportReceipt importReceipt;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "amount", nullable = false)
    private int amount;
}
