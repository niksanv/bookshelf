package ru.bookshelf.exception;

import java.io.Serial;

/**
 * Базовое исключение приложения BookShelf.
 * Непроверяемое: пользовательский интерфейс перехватывает его в одном месте,
 * показывает сообщение и продолжает работу.
 */
public class BookShelfException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BookShelfException(String message) {
        super(message);
    }
}
