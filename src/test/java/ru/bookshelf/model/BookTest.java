package ru.bookshelf.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookTest {

    private static Book book(int id, String title) {
        return new Book(id, title, "Автор", 2000, "Жанр", ReadingStatus.UNREAD);
    }

    @Test
    void storesAllFields() {
        Book book = new Book(7, "Мастер и Маргарита", "М. Булгаков", 1967, "Роман", ReadingStatus.READING);

        assertEquals(7, book.getId());
        assertEquals("Мастер и Маргарита", book.getTitle());
        assertEquals("М. Булгаков", book.getAuthor());
        assertEquals(1967, book.getPublicationYear());
        assertEquals("Роман", book.getGenre());
        assertEquals(ReadingStatus.READING, book.getReadingStatus());
    }

    @Test
    void rejectsNonPositiveId() {
        assertThrows(IllegalArgumentException.class, () -> book(0, "Название"));
        assertThrows(IllegalArgumentException.class, () -> book(-1, "Название"));
    }

    @Test
    void rejectsNullReferenceFields() {
        assertThrows(NullPointerException.class,
                () -> new Book(1, null, "Автор", 2000, "Жанр", ReadingStatus.UNREAD));
        assertThrows(NullPointerException.class,
                () -> new Book(1, "Название", null, 2000, "Жанр", ReadingStatus.UNREAD));
        assertThrows(NullPointerException.class,
                () -> new Book(1, "Название", "Автор", 2000, null, ReadingStatus.UNREAD));
        assertThrows(NullPointerException.class,
                () -> new Book(1, "Название", "Автор", 2000, "Жанр", null));
    }

    @Test
    void identityIsDefinedByIdOnly() {
        assertEquals(book(1, "Первая"), book(1, "Другое название"));
        assertEquals(book(1, "Первая").hashCode(), book(1, "Другое название").hashCode());
        assertNotEquals(book(1, "Первая"), book(2, "Первая"));
    }
}