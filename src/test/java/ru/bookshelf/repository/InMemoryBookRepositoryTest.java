package ru.bookshelf.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.bookshelf.exception.BookNotFoundException;
import ru.bookshelf.exception.DuplicateBookIdException;
import ru.bookshelf.model.Book;
import ru.bookshelf.model.ReadingStatus;
import ru.bookshelf.repository.impl.InMemoryBookRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryBookRepositoryTest {

    private BookRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryBookRepository();
    }

    private static Book book(int id, String title) {
        return new Book(id, title, "Автор", 2000, "Жанр", ReadingStatus.UNREAD);
    }

    // --- nextId ---

    @Test
    void nextIdStartsFromOneAndIncreases() {
        assertEquals(1, repository.nextId());
        assertEquals(2, repository.nextId());
        assertEquals(3, repository.nextId());
    }

    @Test
    void nextIdIsNotReusedAfterDeletion() {
        int id = repository.nextId();
        repository.save(book(id, "Первая"));
        repository.deleteById(id);

        assertEquals(id + 1, repository.nextId());
    }

    @Test
    void nextIdSkipsOverExplicitlySavedLargerId() {
        repository.save(book(10, "С явным ID"));

        assertEquals(11, repository.nextId());
    }

    @Test
    void savingSmallerIdDoesNotLowerCounter() {
        repository.nextId(); // 1
        repository.nextId(); // 2
        repository.save(book(1, "Старый ID"));

        assertEquals(3, repository.nextId());
    }

    // --- save / findById ---

    @Test
    void savedBookCanBeFoundById() {
        Book saved = book(repository.nextId(), "Первая");
        repository.save(saved);

        assertSame(saved, repository.findById(saved.getId()).orElseThrow());
    }

    @Test
    void findByIdReturnsEmptyForMissingBook() {
        assertTrue(repository.findById(42).isEmpty());
    }

    @Test
    void saveRejectsDuplicateIdAndKeepsOriginal() {
        Book original = book(1, "Оригинал");
        repository.save(original);

        DuplicateBookIdException ex = assertThrows(DuplicateBookIdException.class,
                () -> repository.save(book(1, "Дубликат")));

        assertEquals(1, ex.getBookId());
        assertSame(original, repository.findById(1).orElseThrow());
        assertEquals(1, repository.findAll().size());
    }

    // --- findAll ---

    @Test
    void findAllOnEmptyRepositoryReturnsEmptyList() {
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void findAllKeepsInsertionOrder() {
        Book first = book(5, "Первая");
        Book second = book(2, "Вторая");
        Book third = book(9, "Третья");
        repository.save(first);
        repository.save(second);
        repository.save(third);

        List<Book> all = repository.findAll();

        assertEquals(3, all.size());
        assertSame(first, all.get(0));
        assertSame(second, all.get(1));
        assertSame(third, all.get(2));
    }

    @Test
    void findAllReturnsUnmodifiableSnapshot() {
        repository.save(book(1, "Первая"));
        List<Book> snapshot = repository.findAll();

        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(book(2, "Вторая")));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.remove(0));

        repository.save(book(2, "Вторая"));
        assertEquals(1, snapshot.size(), "Ранее полученный список не должен меняться");
        assertEquals(2, repository.findAll().size());
    }

    // --- update ---

    @Test
    void updateReplacesBookAndKeepsPosition() {
        repository.save(book(1, "Первая"));
        repository.save(book(2, "Вторая"));
        repository.save(book(3, "Третья"));
        Book changed = new Book(2, "Новая вторая", "Другой автор", 1999, "Драма", ReadingStatus.READ);

        repository.update(changed);

        assertSame(changed, repository.findById(2).orElseThrow());
        assertEquals(List.of(1, 2, 3),
                repository.findAll().stream().map(Book::getId).toList());
    }

    @Test
    void updateOfMissingBookThrowsAndKeepsStorageUnchanged() {
        repository.save(book(1, "Первая"));

        BookNotFoundException ex = assertThrows(BookNotFoundException.class,
                () -> repository.update(book(99, "Нет такой")));

        assertEquals(99, ex.getBookId());
        assertEquals(1, repository.findAll().size());
        assertTrue(repository.findById(99).isEmpty());
    }

    // --- deleteById ---

    @Test
    void deleteRemovesOnlyTargetBook() {
        repository.save(book(1, "Первая"));
        repository.save(book(2, "Вторая"));

        repository.deleteById(1);

        assertTrue(repository.findById(1).isEmpty());
        assertEquals(List.of(2), repository.findAll().stream().map(Book::getId).toList());
    }

    @Test
    void deleteOfMissingBookThrows() {
        BookNotFoundException ex = assertThrows(BookNotFoundException.class,
                () -> repository.deleteById(5));

        assertEquals(5, ex.getBookId());
        assertEquals("Книга с ID 5 не найдена", ex.getMessage());
    }

    @Test
    void secondDeleteOfSameBookThrows() {
        repository.save(book(1, "Первая"));
        repository.deleteById(1);

        assertThrows(BookNotFoundException.class, () -> repository.deleteById(1));
    }
}