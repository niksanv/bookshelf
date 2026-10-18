package ru.bookshelf.validation;

import ru.bookshelf.exception.ValidationException;
import ru.bookshelf.model.BookInput;

/**
 * Проверка пользовательских данных. Единственное место, где описаны правила валидации.
 * Каждый метод при нарушении бросает {@link ValidationException}.
 */
public interface BookValidator {

    /** ID должен быть положительным числом. */
    void validateId(int id);

    /**
     * Проверяет все поля книги и собирает ВСЕ ошибки сразу:
     * название и автор обязательны, жанр не пуст, длины ограничены,
     * год в допустимом диапазоне, статус указан.
     */
    void validate(BookInput input);

    /** Поисковый запрос не должен быть пустым. */
    void validateSearchQuery(String query);
}