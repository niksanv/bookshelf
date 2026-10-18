package ru.bookshelf.ui;

import java.io.Serial;

/**
 * Поток ввода закончился (Ctrl+D / Ctrl+Z или конец файла при вводе из канала).
 * Это не ошибка пользователя: меню завершает работу штатно.
 */
public class InputEndedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InputEndedException() {
        super("Ввод завершён");
    }
}
