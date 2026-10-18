package ru.bookshelf.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.bookshelf.exception.BookNotFoundException;
import ru.bookshelf.exception.ValidationException;
import ru.bookshelf.model.Book;
import ru.bookshelf.model.BookInput;
import ru.bookshelf.model.ReadingStatus;
import ru.bookshelf.model.SearchField;
import ru.bookshelf.model.SortField;
import ru.bookshelf.repository.BookRepository;
import ru.bookshelf.repository.impl.InMemoryBookRepository;
import ru.bookshelf.service.impl.BookServiceImpl;
import ru.bookshelf.validation.impl.BookValidatorImpl;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Сервис проверяется вместе с настоящими InMemoryBookRepository и BookValidatorImpl:
 * так тесты подтверждают именно взаимодействие слоёв, а не заглушки.
 */
class BookServiceImplTest {

    private BookRepository repository;
    private BookService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryBookRepository();
        Clock fixed = Clock.fixed(Instant.parse("2026-10-18T12:00:00Z"), ZoneOffset.UTC);
        service = new BookServiceImpl(repository, new BookValidatorImpl(fixed));
    }

    private static BookInput input(String title, String author, int year, String genre) {
        return new BookInput(title, author, year, genre, ReadingStatus.UNREAD);
    }

    private static BookInput validInput() {
        return input("Война и мир", "Л. Толстой", 1869, "Роман");
    }

    private static List<Integer> ids(List<Book> books) {
        return books.stream().map(Book::getId).toList();
    }

    private static List<String> titles(List<Book> books) {
        return books.stream().map(Book::getTitle).toList();
    }

    // ---------- addBook ----------

    @Test
    void addBookAssignsSequentialUniqueIds() {
        Book first = service.addBook(validInput());
        Book second = service.addBook(input("Анна Каренина", "Л. Толстой", 1877, "Роман"));

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
        assertSame(first, repository.findById(1).orElseThrow());
    }

    @Test
    void addBookStoresTrimmedValuesAndStatus() {
        Book book = service.addBook(new BookInput("  Дюна ", " Ф. Герберт ", 1965, " Фантастика ", ReadingStatus.READING));

        assertEquals("Дюна", book.getTitle());
        assertEquals("Ф. Герберт", book.getAuthor());
        assertEquals("Фантастика", book.getGenre());
        assertEquals(ReadingStatus.READING, book.getReadingStatus());
    }

    @Test
    void invalidBookIsNotSavedAndDoesNotConsumeId() {
        assertThrows(ValidationException.class, () -> service.addBook(input("", "Автор", 2000, "Жанр")));

        assertTrue(repository.findAll().isEmpty());
        assertEquals(1, service.addBook(validInput()).getId());
    }

    // ---------- getBookById / getAllBooks ----------

    @Test
    void getBookByIdReturnsStoredBook() {
        Book added = service.addBook(validInput());

        assertSame(added, service.getBookById(added.getId()));
    }

    @Test
    void getBookByIdOfMissingBookThrowsNotFound() {
        BookNotFoundException ex = assertThrows(BookNotFoundException.class, () -> service.getBookById(42));

        assertEquals(42, ex.getBookId());
        assertEquals("Книга с ID 42 не найдена", ex.getMessage());
    }

    @Test
    void getBookByIdWithIncorrectIdThrowsValidation() {
        ValidationException ex = assertThrows(ValidationException.class, () -> service.getBookById(0));

        assertTrue(ex.getErrors().get(0).startsWith("ID:"));
    }

    @Test
    void getAllBooksReturnsEmptyListForEmptyCatalog() {
        assertTrue(service.getAllBooks().isEmpty());
    }

    @Test
    void getAllBooksKeepsInsertionOrder() {
        service.addBook(input("Б", "А", 2000, "Ж"));
        service.addBook(input("А", "Б", 1990, "Ж"));

        assertEquals(List.of("Б", "А"), titles(service.getAllBooks()));
    }

    // ---------- updateBook ----------

    @Test
    void updateReplacesDataKeepingIdAndPosition() {
        service.addBook(input("Первая", "А", 2001, "Ж"));
        Book target = service.addBook(input("Вторая", "Б", 2002, "Ж"));
        service.addBook(input("Третья", "В", 2003, "Ж"));

        Book updated = service.updateBook(target.getId(),
                new BookInput(" Новая вторая ", "Новый автор", 1999, "Драма", ReadingStatus.READ));

        assertEquals(target.getId(), updated.getId());
        assertEquals("Новая вторая", updated.getTitle());
        assertEquals("Новый автор", updated.getAuthor());
        assertEquals(1999, updated.getPublicationYear());
        assertEquals("Драма", updated.getGenre());
        assertEquals(ReadingStatus.READ, updated.getReadingStatus());
        assertSame(updated, service.getBookById(target.getId()));
        assertEquals(List.of(1, 2, 3), ids(service.getAllBooks()));
    }

    @Test
    void updateDoesNotCreateNewBookOrConsumeId() {
        Book added = service.addBook(validInput());

        service.updateBook(added.getId(), input("Другое", "Другой", 2000, "Жанр"));

        assertEquals(1, service.getAllBooks().size());
        assertEquals(2, service.addBook(validInput()).getId());
    }

    @Test
    void updateWithInvalidDataKeepsStoredBookUnchanged() {
        Book added = service.addBook(validInput());

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.updateBook(added.getId(), input("", "", 5000, "")));

        assertEquals(4, ex.getErrors().size());
        assertSame(added, service.getBookById(added.getId()));
    }

    @Test
    void updateOfMissingBookThrowsNotFoundAndCreatesNothing() {
        assertThrows(BookNotFoundException.class, () -> service.updateBook(7, validInput()));

        assertTrue(service.getAllBooks().isEmpty());
    }

    @Test
    void updateWithIncorrectIdThrowsValidation() {
        assertThrows(ValidationException.class, () -> service.updateBook(-3, validInput()));
    }

    // ---------- deleteBook ----------

    @Test
    void deleteRemovesBookAndDoesNotReuseId() {
        Book first = service.addBook(validInput());
        service.addBook(input("Вторая", "Автор", 2000, "Жанр"));

        service.deleteBook(first.getId());

        assertEquals(List.of(2), ids(service.getAllBooks()));
        assertThrows(BookNotFoundException.class, () -> service.getBookById(first.getId()));
        assertEquals(3, service.addBook(validInput()).getId());
    }

    @Test
    void deleteOfMissingBookThrowsNotFound() {
        BookNotFoundException ex = assertThrows(BookNotFoundException.class, () -> service.deleteBook(5));

        assertEquals(5, ex.getBookId());
    }

    @Test
    void deleteWithIncorrectIdThrowsValidation() {
        assertThrows(ValidationException.class, () -> service.deleteBook(0));
    }

    // ---------- searchBooks ----------

    private void addSearchFixtures() {
        service.addBook(input("Война и мир", "Лев Толстой", 1869, "Роман"));
        service.addBook(input("Анна Каренина", "Лев Толстой", 1877, "Роман"));
        service.addBook(input("Мастер и Маргарита", "Михаил Булгаков", 1967, "Фантастика"));
    }

    @Test
    void searchByTitleIsCaseInsensitiveAndPartial() {
        addSearchFixtures();

        assertEquals(List.of(1), ids(service.searchBooks(SearchField.TITLE, "ВОЙНА")));
        assertEquals(List.of(1), ids(service.searchBooks(SearchField.TITLE, "и мир")));
        assertEquals(List.of(3), ids(service.searchBooks(SearchField.TITLE, "МАР")));
    }

    @Test
    void searchIgnoresWhitespaceAroundQuery() {
        addSearchFixtures();

        assertEquals(List.of(1), ids(service.searchBooks(SearchField.TITLE, "  война  ")));
    }

    @Test
    void searchByAuthorFindsPartialMatches() {
        addSearchFixtures();

        assertEquals(List.of(1, 2), ids(service.searchBooks(SearchField.AUTHOR, "толст")));
        assertEquals(List.of(3), ids(service.searchBooks(SearchField.AUTHOR, "БУЛГАКОВ")));
    }

    @Test
    void searchByGenreFindsPartialMatches() {
        addSearchFixtures();

        assertEquals(List.of(1, 2), ids(service.searchBooks(SearchField.GENRE, "роман")));
        assertEquals(List.of(3), ids(service.searchBooks(SearchField.GENRE, "фант")));
    }

    @Test
    void searchLooksOnlyInTheSelectedField() {
        addSearchFixtures();

        assertTrue(service.searchBooks(SearchField.TITLE, "Толстой").isEmpty());
        assertTrue(service.searchBooks(SearchField.GENRE, "Война").isEmpty());
    }

    @Test
    void searchWithoutMatchesReturnsEmptyList() {
        addSearchFixtures();

        assertTrue(service.searchBooks(SearchField.TITLE, "несуществующее").isEmpty());
    }

    @Test
    void searchWithBlankQueryThrowsValidation() {
        addSearchFixtures();

        for (String query : new String[]{null, "", "   "}) {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> service.searchBooks(SearchField.TITLE, query));
            assertTrue(ex.getErrors().get(0).startsWith("Поисковый запрос"));
        }
    }

    @Test
    void searchWithNullFieldIsProgrammingError() {
        assertThrows(NullPointerException.class, () -> service.searchBooks(null, "война"));
    }

    // ---------- getAllBooksSorted ----------

    @Test
    void sortByTitleIgnoresCase() {
        service.addBook(input("вишня", "А", 2000, "Ж"));
        service.addBook(input("Арбуз", "Б", 2000, "Ж"));
        service.addBook(input("банан", "В", 2000, "Ж"));

        assertEquals(List.of("Арбуз", "банан", "вишня"),
                titles(service.getAllBooksSorted(SortField.TITLE)));
    }

    @Test
    void sortByAuthorIgnoresCase() {
        service.addBook(input("1", "яблонский", 2000, "Ж"));
        service.addBook(input("2", "Александров", 2000, "Ж"));
        service.addBook(input("3", "Борисов", 2000, "Ж"));

        assertEquals(List.of("Александров", "Борисов", "яблонский"),
                service.getAllBooksSorted(SortField.AUTHOR).stream().map(Book::getAuthor).toList());
    }

    @Test
    void sortByYearIsNumericNotTextual() {
        service.addBook(input("Поздняя", "А", 2005, "Ж"));
        service.addBook(input("Древняя", "Б", 987, "Ж"));
        service.addBook(input("Средняя", "В", 1999, "Ж"));

        assertEquals(List.of(987, 1999, 2005),
                service.getAllBooksSorted(SortField.YEAR).stream().map(Book::getPublicationYear).toList());
    }

    @Test
    void sortUsesStableTieBreakers() {
        service.addBook(input("Б", "Один", 2000, "Ж"));   // id 1
        service.addBook(input("А", "Один", 2000, "Ж"));   // id 2
        service.addBook(input("А", "Один", 2000, "Ж"));   // id 3, полностью совпадает с id 2

        assertEquals(List.of(2, 3, 1), ids(service.getAllBooksSorted(SortField.YEAR)));
        assertEquals(List.of(2, 3, 1), ids(service.getAllBooksSorted(SortField.AUTHOR)));
    }

    @Test
    void sortingDoesNotChangeRepositoryOrder() {
        service.addBook(input("В", "Я", 2003, "Ж"));
        service.addBook(input("А", "Б", 2001, "Ж"));
        service.addBook(input("Б", "А", 2002, "Ж"));

        service.getAllBooksSorted(SortField.TITLE);
        service.getAllBooksSorted(SortField.YEAR);

        assertEquals(List.of(1, 2, 3), ids(repository.findAll()));
        assertEquals(List.of(1, 2, 3), ids(service.getAllBooks()));
    }

    @Test
    void sortedListIsUnmodifiable() {
        service.addBook(validInput());
        List<Book> sorted = service.getAllBooksSorted(SortField.TITLE);

        assertThrows(UnsupportedOperationException.class, () -> sorted.remove(0));
    }

    @Test
    void sortWithNullFieldIsProgrammingError() {
        assertThrows(NullPointerException.class, () -> service.getAllBooksSorted(null));
    }
}