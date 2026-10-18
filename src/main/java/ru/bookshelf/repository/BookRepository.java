package ru.bookshelf.repository;

import ru.bookshelf.exception.BookNotFoundException;
import ru.bookshelf.exception.DuplicateBookIdException;
import ru.bookshelf.model.Book;

import java.util.List;
import java.util.Optional;

/**
 * Хранилище книг. Отвечает только за хранение и выдачу данных:
 * валидации, поиска, сортировки и форматирования здесь нет.
 */
public interface BookRepository {

    /**
     * Выдаёт новый уникальный ID. Каждый вызов возвращает новое значение,
     * даже если книга с этим ID так и не была сохранена. ID освободившихся
     * после удаления книг повторно не выдаются.
     */
    int nextId();

    /**
     * Добавляет новую книгу.
     *
     * @throws DuplicateBookIdException если книга с таким ID уже есть (хранилище не изменяется)
     */
    void save(Book book);

    /**
     * Возвращает книгу по ID или {@link Optional#empty()}, если её нет.
     */
    Optional<Book> findById(int id);

    /**
     * Возвращает неизменяемый снимок всех книг в порядке добавления.
     * Последующие изменения хранилища на уже полученный список не влияют.
     */
    List<Book> findAll();

    /**
     * Заменяет сохранённую книгу с тем же ID. Положение книги в списке не меняется.
     *
     * @throws BookNotFoundException если книги с таким ID нет (хранилище не изменяется)
     */
    void update(Book book);

    /**
     * Удаляет книгу по ID.
     *
     * @throws BookNotFoundException если книги с таким ID нет
     */
    void deleteById(int id);
}