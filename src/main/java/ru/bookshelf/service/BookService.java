package ru.bookshelf.service;

import ru.bookshelf.exception.BookNotFoundException;
import ru.bookshelf.exception.ValidationException;
import ru.bookshelf.model.Book;
import ru.bookshelf.model.BookInput;
import ru.bookshelf.model.SearchField;
import ru.bookshelf.model.SortField;

import java.util.List;

/**
 * Бизнес-логика библиотеки. Интерфейс для пользовательского слоя: всё, что нужно меню.
 * <p>
 * Исключения предсказуемы: некорректные данные или ID ≤ 0 — {@link ValidationException},
 * корректный, но отсутствующий ID — {@link BookNotFoundException}.
 * Передача {@code null} вместо объекта ({@code input}, {@code field}, {@code sortField}) —
 * ошибка программиста и приводит к {@link NullPointerException}.
 */
public interface BookService {

    /** Проверяет данные, назначает уникальный ID, сохраняет и возвращает книгу. */
    Book addBook(BookInput input);

    /** Возвращает книгу по ID. */
    Book getBookById(int id);

    /** Все книги в порядке добавления. Возвращаемый список неизменяем. */
    List<Book> getAllBooks();

    /** Все книги, отсортированные по указанному полю. Порядок хранилища не меняется. */
    List<Book> getAllBooksSorted(SortField sortField);

    /**
     * Заменяет данные существующей книги, ID и положение в списке сохраняются.
     * Порядок проверок: формат ID, данные, существование книги.
     */
    Book updateBook(int id, BookInput input);

    /** Удаляет книгу по ID. */
    void deleteBook(int id);

    /**
     * Ищет книги, у которых выбранное поле содержит запрос (частичное совпадение,
     * без учёта регистра, пробелы по краям запроса игнорируются).
     * Ничего не найдено — это пустой список, а не ошибка.
     */
    List<Book> searchBooks(SearchField field, String query);
}