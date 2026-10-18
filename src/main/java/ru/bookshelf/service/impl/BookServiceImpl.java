package ru.bookshelf.service.impl;

import ru.bookshelf.exception.BookNotFoundException;
import ru.bookshelf.model.Book;
import ru.bookshelf.model.BookInput;
import ru.bookshelf.model.SearchField;
import ru.bookshelf.model.SortField;
import ru.bookshelf.repository.BookRepository;
import ru.bookshelf.service.BookService;
import ru.bookshelf.validation.BookValidator;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Реализация бизнес-логики. Зависимости передаются через конструктор,
 * собственного состояния (и тем более static-состояния) у сервиса нет:
 * каталог целиком живёт в {@link BookRepository}.
 */
public class BookServiceImpl implements BookService {

    private final BookRepository repository;
    private final BookValidator validator;

    public BookServiceImpl(BookRepository repository, BookValidator validator) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.validator = Objects.requireNonNull(validator, "validator");
    }

    @Override
    public Book addBook(BookInput input) {
        validator.validate(input);                 // сначала проверка, чтобы не «сжигать» ID впустую
        Book book = toBook(repository.nextId(), input);
        repository.save(book);
        return book;
    }

    @Override
    public Book getBookById(int id) {
        validator.validateId(id);
        return repository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    @Override
    public List<Book> getAllBooks() {
        return repository.findAll();
    }

    @Override
    public List<Book> getAllBooksSorted(SortField sortField) {
        Objects.requireNonNull(sortField, "sortField");
        // findAll() отдаёт снимок; сортируется его копия, хранилище не затрагивается
        return repository.findAll().stream()
                .sorted(sortField.comparator())
                .toList();
    }

    @Override
    public Book updateBook(int id, BookInput input) {
        validator.validateId(id);
        validator.validate(input);
        Book updated = toBook(id, input);
        repository.update(updated);                // BookNotFoundException, если книги нет
        return updated;
    }

    @Override
    public void deleteBook(int id) {
        validator.validateId(id);
        repository.deleteById(id);                 // BookNotFoundException, если книги нет
    }

    @Override
    public List<Book> searchBooks(SearchField field, String query) {
        Objects.requireNonNull(field, "field");
        validator.validateSearchQuery(query);
        String needle = normalize(query);
        return repository.findAll().stream()
                .filter(book -> normalize(field.extract(book)).contains(needle))
                .toList();
    }

    private static Book toBook(int id, BookInput input) {
        return new Book(id,
                input.title(),
                input.author(),
                input.publicationYear(),
                input.genre(),
                input.readingStatus());
    }

    private static String normalize(String text) {
        return text.strip().toLowerCase(Locale.ROOT);
    }
}