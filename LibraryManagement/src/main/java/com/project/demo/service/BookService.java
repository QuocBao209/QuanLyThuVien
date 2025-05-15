package com.project.demo.service;

import com.project.demo.entity.Author;
import com.project.demo.entity.Book;
import com.project.demo.entity.Category;
import com.project.demo.repository.BookRepository;
import com.project.demo.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;


    // Tìm ID book theo kiểu dữ liệu Book
    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }
    
    // Tìm ID book theo kiểu dữ liệu Optional (Bảo) : dùng để xử lý "Mượn sách"
    public Optional<Book> getOptionalBookById(Long id) {
        return bookRepository.findById(id);
    }


    // Lấy danh sách tất cả Book (kiểu List)
    public List<Book> getBooks() {
        return bookRepository.findByIsDeletedFalse();
    }

    // Xử lý lọc thông tin trả về dữ liệu Page do làm danh sách phân trang (đang để mặc định mỗi trang có 20 sách)
//    public Page<Book> filterBooks(Set<String> categoryNames, Set<String> timeRanges, int page, int size) {
//    	Pageable pageable = PageRequest.of(page, size);				// Bảo
//        List<Book> books = bookRepository.findByIsDeletedFalse();
//
//        // Lọc theo danh mục
//        if (categoryNames != null && !categoryNames.isEmpty()) {
//            books = books.stream()
//                    .filter(book -> categoryNames.contains(book.getCategory().getCategoryName()))
//                    .collect(Collectors.toList());
//        }
//
//        // Lọc theo khoảng thời gian xuất bản
//        if (timeRanges != null && !timeRanges.isEmpty()) {
//            books = books.stream()
//                    .filter(book -> timeRanges.stream().anyMatch(range -> isWithinYearRange(book.getPublishYear(), range)))
//                    .collect(Collectors.toList());
//        }
//
//        // Chuyển danh sách đã lọc thành Page<Book> (Bảo)
//        int start = (int) pageable.getOffset();
//        int end = Math.min((start + pageable.getPageSize()), books.size());
//        List<Book> pagedBooks = books.subList(start, end);
//
//        return new PageImpl<>(pagedBooks, pageable, books.size());
//    }
    
    public Page<Book> filterBooks(Set<String> categoryNames, String timeRange, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Book> books = bookRepository.findByIsDeletedFalse();

        // Lọc theo danh mục
        if (categoryNames != null && !categoryNames.isEmpty()) {
            books = books.stream()
                    .filter(book -> categoryNames.contains(book.getCategory().getCategoryName()))
                    .collect(Collectors.toList());
        }

        // Lọc theo khoảng thời gian xuất bản
        if (timeRange != null && !timeRange.isEmpty()) {
            books = books.stream()
                    .filter(book -> isWithinYearRange(book.getPublishYear(), timeRange))
                    .collect(Collectors.toList());
        }

        // Lọc theo từ khóa (tìm kiếm trong tiêu đề sách)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchKeyword = keyword.trim().toLowerCase();
            books = books.stream()
                    .filter(book -> book.getBookName().toLowerCase().contains(searchKeyword))
                    .collect(Collectors.toList());
        }

        // Chuyển danh sách đã lọc thành Page<Book>
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), books.size());
        List<Book> pagedBooks = books.subList(start, end);

        return new PageImpl<>(pagedBooks, pageable, books.size());
    }

    private boolean isWithinYearRange(int year, String range) {
        String[] years = range.split("-");
        if (years.length == 2) {
            int startYear = Integer.parseInt(years[0]);
            int endYear = Integer.parseInt(years[1]);
            return year >= startYear && year <= endYear;
        }
        return false;
    }
    
    // Chuyển dữ liệu danh sách sách vào database
    public void transferData(List<Book> books) {
        bookRepository.saveAll(books);
    }

    // Phân trang danh sách theo tác giả ở bookDetail (Bảo)
    public Page<Book> getBooksByAuthors(List<Author> authors, Long excludeBookId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findBooksByAuthors(authors, excludeBookId, pageable);
    }
    
    // Phân trang danh sách tất cả sách ở bookFilter (kiểu dữ liệu Page) (Bảo)
    public Page<Book> getAllBooks(Pageable pageable) {
        return bookRepository.findByIsDeletedFalse(pageable);
    }
    
    // Phân trang danh sách sách mới nhất với bộ lọc danh mục và từ khóa
    public Page<Book> getLatestBooks(Set<String> categoryNames, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishYear").descending());
        List<Book> books = bookRepository.findByIsDeletedFalse();

        // Lọc theo danh mục
        if (categoryNames != null && !categoryNames.isEmpty()) {
            books = books.stream()
                    .filter(book -> categoryNames.contains(book.getCategory().getCategoryName()))
                    .collect(Collectors.toList());
        }
        
     // Lọc theo từ khóa (tìm kiếm trong tiêu đề sách)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchKeyword = keyword.trim().toLowerCase();
            books = books.stream()
                    .filter(book -> book.getBookName().toLowerCase().contains(searchKeyword))
                    .collect(Collectors.toList());
        }

        // Chuyển danh sách đã lọc thành Page<Book>
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), books.size());
        List<Book> pagedBooks = books.subList(start, end);

        return new PageImpl<>(pagedBooks, pageable, books.size());
    }
    
    //Phân trang danh sách sách đề cử ( không lấy top 3) (An)
    public Page<Book> getPagedBooksSortedByBorrowCountExcludingTop3(int page, int size) {
        List<Book> top3Books = getTop3Books(); // Lấy danh sách top 3
        
        Page<Book> pagedBooks = bookRepository.findBooksSortedByBorrowCount(PageRequest.of(page, size + 3)); // Lấy thêm 3 để tránh mất sách khi lọc

        List<Book> filteredBooks = pagedBooks.stream()
            .filter(book -> !top3Books.contains(book))
            .toList();

        long totalBooksExcludingTop3 = bookRepository.count() - top3Books.size(); // Đếm tổng số sách trừ top 3

        return new PageImpl<>(filteredBooks, PageRequest.of(page, size), totalBooksExcludingTop3);
    }
    //Top 3 sách đề cử(An)
    public List<Book> getTop3Books() {
        return bookRepository.findBooksSortedByBorrowCount(PageRequest.of(0, 3)).getContent();
    }
}