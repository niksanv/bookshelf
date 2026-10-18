package ru.bookshelf.repository.impl;

import ru.bookshelf.exception.BookNotFoundException;
import ru.bookshelf.exception.DuplicateBookIdException;
import ru.bookshelf.model.Book;
import ru.bookshelf.repository.BookRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Хранилище книг в оперативной памяти.
 * <p>
 * Данные лежат в {@link LinkedHashMap}: поиск по ID за O(1) и стабильный порядок
 * добавления. Коллекция приватна и наружу не передаётся; поскольку {@link Book}
 * неизменяема, защитное копирование отдельных книг не требуется.
 * <p>
 * Класс не рассчитан на многопоточное использование (консольное приложение
 * однопоточное).
 */
public class InMemoryBookRepository implements BookRepository {

    private final Map<Integer, Book> books = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public int nextId() {
        return nextId++;
    }

    @Override
    public void save(Book book) {
        Objects.requireNonNull(book, "book");
        int id = book.getId();
        if (books.containsKey(id)) {
            throw new DuplicateBookIdException(id);
        }
        books.put(id, book);
        // Счётчик никогда не отстаёт от самого большого сохранённого ID,
        // поэтому nextId() не выдаст уже занятый номер.
        if (id >= nextId) {
            nextId = id + 1;
        }
    }

    @Override
    public Optional<Book> findById(int id) {
        return Optional.ofNullable(books.get(id));
    }

    @Override
    public List<Book> findAll() {
        return List.copyOf(books.values());
    }

    @Override
    public void update(Book book) {
        Objects.requireNonNull(book, "book");
        int id = book.getId();
        if (!books.containsKey(id)) {
            throw new BookNotFoundException(id);
        }
        books.put(id, book); // замена значения не меняет порядок в LinkedHashMap
    }

    @Override
    public void deleteById(int id) {
        if (books.remove(id) == null) {
            throw new BookNotFoundException(id);
        }
    }
}