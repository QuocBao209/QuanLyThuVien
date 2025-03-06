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
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    public Optional<Book> findByBookNameAndAuthors(String bookName, List<Author> authors) {
        return bookRepository.findByBookNameAndIsDeletedFalse(bookName).stream()
                .filter(book -> new HashSet<>(book.getAuthors()).containsAll(authors))
                .findFirst();
    }

    public Optional<Book> findExactMatch(String bookName, List<Author> authors, Category category, int publishYear) {
        return bookRepository.findByIsDeletedFalse().stream()
                .filter(b -> b.getBookName().equalsIgnoreCase(bookName) &&
                        b.getPublishYear() == publishYear &&
                        Objects.equals(b.getCategory(), category) &&
                        new HashSet<>(b.getAuthors()).equals(new HashSet<>(authors)))
                .findFirst();
    }

    public List<Book> searchBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return bookRepository.findByIsDeletedFalse();
        }
        return bookRepository.findByBookNameContainingIgnoreCaseAndIsDeletedFalseOrAuthors_AuthorNameContainingIgnoreCaseAndIsDeletedFalse(keyword, keyword);
    }
    
    // Tìm ID book theo kiểu dữ liệu Book
    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }
    
    // Tìm ID book theo kiểu dữ liệu Optional (Bảo) : dùng để xử lý "Mượn sách"
    public Optional<Book> getOptionalBookById(Long id) {
        return bookRepository.findById(id);
    }

    // Lưu sách mới hoặc cập nhật sách
    public void saveBook(Book book) {
        bookRepository.save(book);
    }

    // Cập nhật thông tin sách (nếu tồn tại)
    public void updateBook(Book book) {
        if (bookRepository.existsById(book.getBookId())) {
            bookRepository.save(book);
        }
    }
    public String getBookImagePath(Long bookId) {
        return bookRepository.findBookImagePathById(bookId);
    }

    // Xóa sách theo ID
    public void deleteBook(Long id) {
        bookRepository.findById(id).ifPresent(book -> {
            book.setDeleted(true);
            bookRepository.save(book);
        });
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
    
    public Page<Book> filterBooks(Set<String> categoryNames, String timeRange, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Book> books = bookRepository.findByIsDeletedFalse();

        // Lọc theo danh mục
        if (categoryNames != null && !categoryNames.isEmpty()) {
            books = books.stream()
                    .filter(book -> categoryNames.contains(book.getCategory().getCategoryName()))
                    .collect(Collectors.toList());
        }

        // Lọc theo khoảng thời gian xuất bản (Chỉ 1 khoảng thay vì danh sách)
        if (timeRange != null && !timeRange.isEmpty()) {
            books = books.stream()
                    .filter(book -> isWithinYearRange(book.getPublishYear(), timeRange))
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
}
