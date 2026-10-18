package ru.bookshelf.exception;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationExceptionTest {

    @Test
    void messageContainsAllErrors() {
        ValidationException ex = new ValidationException(List.of("Название: пусто", "Автор: пусто"));

        assertTrue(ex.getMessage().contains("Название: пусто"));
        assertTrue(ex.getMessage().contains("Автор: пусто"));
        assertEquals(2, ex.getErrors().size());
    }

    @Test
    void errorsAreCopiedAndUnmodifiable() {
        List<String> source = new ArrayList<>(List.of("Название: пусто"));
        ValidationException ex = new ValidationException(source);
        source.add("Автор: пусто");

        assertEquals(1, ex.getErrors().size());
        assertThrows(UnsupportedOperationException.class, () -> ex.getErrors().add("ещё"));
    }

    @Test
    void emptyErrorListIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ValidationException(List.of()));
    }
}