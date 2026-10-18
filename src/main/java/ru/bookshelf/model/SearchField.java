package ru.bookshelf.model;

import java.util.function.Function;

/**
 * Поле книги, по которому выполняется поиск.
 */
public enum SearchField {
    TITLE(Book::getTitle),
    AUTHOR(Book::getAuthor),
    GENRE(Book::getGenre);

    private final Function<Book, String> extractor;

    SearchField(Function<Book, String> extractor) {
        this.extractor = extractor;
    }

    /** Возвращает значение этого поля у книги. */
    public String extract(Book book) {
        return extractor.apply(book);
    }
}