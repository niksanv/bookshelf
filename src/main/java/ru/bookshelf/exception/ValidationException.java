package ru.bookshelf.exception;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

/**
 * Данные не прошли проверку. Хранит все найденные ошибки сразу, чтобы пользователь
 * мог исправить их за один раз. Каждая ошибка называет поле и причину.
 */
@SuppressWarnings("serial")
public class ValidationException extends BookShelfException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super(buildMessage(errors));
        this.errors = List.copyOf(errors);
    }

    /** Неизменяемый список сообщений об ошибках, по одному на каждое нарушенное правило. */
    public List<String> getErrors() {
        return errors;
    }

    private static String buildMessage(List<String> errors) {
        Objects.requireNonNull(errors, "errors");
        if (errors.isEmpty()) {
            throw new IllegalArgumentException("Список ошибок валидации не может быть пустым");
        }
        return "Некорректные данные: " + String.join("; ", errors);
    }
}
