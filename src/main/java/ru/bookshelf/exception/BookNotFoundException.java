package ru.bookshelf.exception;

import java.io.Serial;

/**
 * Книга с указанным ID отсутствует в хранилище.
 */
public class BookNotFoundException extends BookShelfException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int bookId;

    public BookNotFoundException(int bookId) {
        super("Книга с ID " + bookId + " не найдена");
        this.bookId = bookId;
    }

    public int getBookId() {
        return bookId;
    }
}
