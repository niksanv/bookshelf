package ru.bookshelf.exception;

import java.io.Serial;

/**
 * Попытка сохранить книгу с ID, который уже занят другой книгой.
 */
public class DuplicateBookIdException extends BookShelfException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int bookId;

    public DuplicateBookIdException(int bookId) {
        super("Книга с ID " + bookId + " уже существует");
        this.bookId = bookId;
    }

    public int getBookId() {
        return bookId;
    }
}
