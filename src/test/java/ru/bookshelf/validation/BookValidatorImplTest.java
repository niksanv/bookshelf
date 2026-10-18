package ru.bookshelf.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.bookshelf.exception.ValidationException;
import ru.bookshelf.model.BookInput;
import ru.bookshelf.model.ReadingStatus;
import ru.bookshelf.validation.impl.BookValidatorImpl;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookValidatorImplTest {

    private static final int CURRENT_YEAR = 2026;

    private BookValidator validator;

    @BeforeEach
    void setUp() {
        Clock fixed = Clock.fixed(Instant.parse("2026-10-18T12:00:00Z"), ZoneOffset.UTC);
        validator = new BookValidatorImpl(fixed);
    }

    private static BookInput input(String title, String author, int year, String genre, ReadingStatus status) {
        return new BookInput(title, author, year, genre, status);
    }

    private static BookInput valid() {
        return input("Война и мир", "Л. Толстой", 1869, "Роман", ReadingStatus.UNREAD);
    }

    private ValidationException errorsFor(BookInput input) {
        return assertThrows(ValidationException.class, () -> validator.validate(input));
    }

    // --- validate: успешные случаи ---

    @Test
    void acceptsValidBook() {
        assertDoesNotThrow(() -> validator.validate(valid()));
    }

    @Test
    void acceptsBoundaryValues() {
        assertDoesNotThrow(() -> validator.validate(
                input("а".repeat(ValidationRules.MAX_TITLE_LENGTH),
                        "б".repeat(ValidationRules.MAX_AUTHOR_LENGTH),
                        ValidationRules.MIN_PUBLICATION_YEAR,
                        "в".repeat(ValidationRules.MAX_GENRE_LENGTH),
                        ReadingStatus.READ)));
        assertDoesNotThrow(() -> validator.validate(
                input("Название", "Автор", CURRENT_YEAR, "Жанр", ReadingStatus.READING)));
    }

    // --- обязательные поля ---

    @Test
    void rejectsMissingTitle() {
        for (String title : new String[]{null, "", "   "}) {
            ValidationException ex = errorsFor(input(title, "Автор", 2000, "Жанр", ReadingStatus.UNREAD));
            assertEquals(1, ex.getErrors().size());
            assertEquals("Название: не может быть пустым", ex.getErrors().get(0));
        }
    }

    @Test
    void rejectsMissingAuthor() {
        for (String author : new String[]{null, "", "   "}) {
            ValidationException ex = errorsFor(input("Название", author, 2000, "Жанр", ReadingStatus.UNREAD));
            assertEquals(List.of("Автор: не может быть пустым"), ex.getErrors());
        }
    }

    @Test
    void rejectsEmptyGenre() {
        for (String genre : new String[]{null, "", "   "}) {
            ValidationException ex = errorsFor(input("Название", "Автор", 2000, genre, ReadingStatus.UNREAD));
            assertEquals(List.of("Жанр: не может быть пустым"), ex.getErrors());
        }
    }

    @Test
    void rejectsMissingStatus() {
        ValidationException ex = errorsFor(input("Название", "Автор", 2000, "Жанр", null));

        assertEquals(List.of("Статус прочтения: не указан"), ex.getErrors());
    }

    // --- длины ---

    @Test
    void rejectsTooLongTextFieldsAndNamesTheField() {
        ValidationException ex = errorsFor(input(
                "а".repeat(ValidationRules.MAX_TITLE_LENGTH + 1),
                "б".repeat(ValidationRules.MAX_AUTHOR_LENGTH + 1),
                2000,
                "в".repeat(ValidationRules.MAX_GENRE_LENGTH + 1),
                ReadingStatus.UNREAD));

        assertEquals(3, ex.getErrors().size());
        assertTrue(ex.getErrors().get(0).startsWith("Название: слишком длинное"));
        assertTrue(ex.getErrors().get(0).contains("максимум 200"));
        assertTrue(ex.getErrors().get(1).startsWith("Автор: слишком длинное"));
        assertTrue(ex.getErrors().get(2).startsWith("Жанр: слишком длинное"));
    }

    // --- год ---

    @Test
    void rejectsYearOutsideRange() {
        for (int year : new int[]{0, -500, CURRENT_YEAR + 1, 9999}) {
            ValidationException ex = errorsFor(input("Название", "Автор", year, "Жанр", ReadingStatus.UNREAD));
            assertEquals(1, ex.getErrors().size());
            assertTrue(ex.getErrors().get(0).startsWith("Год публикации: должен быть от 1 до 2026"),
                    ex.getErrors().get(0));
            assertTrue(ex.getErrors().get(0).contains("получено " + year));
        }
    }

    // --- несколько ошибок сразу ---

    @Test
    void collectsAllErrorsAtOnce() {
        ValidationException ex = errorsFor(input("", null, 3000, " ", null));

        assertEquals(5, ex.getErrors().size());
        assertTrue(ex.getErrors().get(0).startsWith("Название"));
        assertTrue(ex.getErrors().get(1).startsWith("Автор"));
        assertTrue(ex.getErrors().get(2).startsWith("Жанр"));
        assertTrue(ex.getErrors().get(3).startsWith("Год публикации"));
        assertTrue(ex.getErrors().get(4).startsWith("Статус прочтения"));
    }

    @Test
    void rejectsNullInputAsProgrammingError() {
        assertThrows(NullPointerException.class, () -> validator.validate(null));
    }

    // --- validateId ---

    @Test
    void acceptsPositiveId() {
        assertDoesNotThrow(() -> validator.validateId(1));
        assertDoesNotThrow(() -> validator.validateId(Integer.MAX_VALUE));
    }

    @Test
    void rejectsNonPositiveId() {
        for (int id : new int[]{0, -1, Integer.MIN_VALUE}) {
            ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateId(id));
            assertEquals(1, ex.getErrors().size());
            assertTrue(ex.getErrors().get(0).startsWith("ID: должен быть положительным числом"));
            assertTrue(ex.getErrors().get(0).contains("получено " + id));
        }
    }

    // --- validateSearchQuery ---

    @Test
    void acceptsNonBlankSearchQuery() {
        assertDoesNotThrow(() -> validator.validateSearchQuery("война"));
        assertDoesNotThrow(() -> validator.validateSearchQuery("  а  "));
    }

    @Test
    void rejectsBlankSearchQuery() {
        for (String query : new String[]{null, "", "   "}) {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> validator.validateSearchQuery(query));
            assertEquals(List.of("Поисковый запрос: не может быть пустым"), ex.getErrors());
        }
    }
}