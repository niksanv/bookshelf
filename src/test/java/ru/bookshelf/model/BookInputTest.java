package ru.bookshelf.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BookInputTest {

    @Test
    void stripsWhitespaceAroundTextFields() {
        BookInput input = new BookInput("  Война и мир ", "\tЛ. Толстой\n", 1869, " Роман ", ReadingStatus.READ);

        assertEquals("Война и мир", input.title());
        assertEquals("Л. Толстой", input.author());
        assertEquals("Роман", input.genre());
    }

    @Test
    void blankStringBecomesEmpty() {
        BookInput input = new BookInput("   ", "", 2000, "  ", ReadingStatus.UNREAD);

        assertEquals("", input.title());
        assertEquals("", input.author());
        assertEquals("", input.genre());
    }

    @Test
    void nullIsKeptForValidatorToReport() {
        BookInput input = new BookInput(null, null, 2000, null, null);

        assertNull(input.title());
        assertNull(input.author());
        assertNull(input.genre());
        assertNull(input.readingStatus());
    }
}