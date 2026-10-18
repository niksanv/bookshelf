package ru.bookshelf.validation.impl;

import ru.bookshelf.exception.ValidationException;
import ru.bookshelf.model.BookInput;
import ru.bookshelf.validation.BookValidator;

import java.time.Clock;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ru.bookshelf.validation.ValidationRules.MAX_AUTHOR_LENGTH;
import static ru.bookshelf.validation.ValidationRules.MAX_GENRE_LENGTH;
import static ru.bookshelf.validation.ValidationRules.MAX_TITLE_LENGTH;
import static ru.bookshelf.validation.ValidationRules.MIN_PUBLICATION_YEAR;

/**
 * Стандартная реализация правил валидации BookShelf.
 * Верхняя граница года — текущий год по переданным часам (в тестах часы фиксируются).
 */
public class BookValidatorImpl implements BookValidator {

    private final Clock clock;

    public BookValidatorImpl() {
        this(Clock.systemDefaultZone());
    }

    public BookValidatorImpl(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public void validateId(int id) {
        if (id <= 0) {
            throw new ValidationException(
                    List.of("ID: должен быть положительным числом (получено " + id + ")"));
        }
    }

    @Override
    public void validate(BookInput input) {
        Objects.requireNonNull(input, "input");
        List<String> errors = new ArrayList<>();

        checkText("Название", input.title(), MAX_TITLE_LENGTH, errors);
        checkText("Автор", input.author(), MAX_AUTHOR_LENGTH, errors);
        checkText("Жанр", input.genre(), MAX_GENRE_LENGTH, errors);
        checkYear(input.publicationYear(), errors);
        if (input.readingStatus() == null) {
            errors.add("Статус прочтения: не указан");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    @Override
    public void validateSearchQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new ValidationException(List.of("Поисковый запрос: не может быть пустым"));
        }
    }

    private static void checkText(String fieldName, String value, int maxLength, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(fieldName + ": не может быть пустым");
        } else if (value.length() > maxLength) {
            errors.add(fieldName + ": слишком длинное, максимум " + maxLength
                    + " символов (введено " + value.length() + ")");
        }
    }

    private void checkYear(int year, List<String> errors) {
        int maxYear = Year.now(clock).getValue();
        if (year < MIN_PUBLICATION_YEAR || year > maxYear) {
            errors.add("Год публикации: должен быть от " + MIN_PUBLICATION_YEAR
                    + " до " + maxYear + " (получено " + year + ")");
        }
    }
}