package ru.bookshelf.model;

import java.util.Comparator;

/**
 * Поле, по которому сортируется список книг (по возрастанию).
 * <p>
 * Тексты сравниваются без учёта регистра, год — как число. Для одинаковых значений
 * заданы запасные критерии, а в конце всегда ID, поэтому порядок полностью детерминирован.
 */
public enum SortField {

    TITLE(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Book::getAuthor, String.CASE_INSENSITIVE_ORDER)
            .thenComparingInt(Book::getId)),

    AUTHOR(Comparator.comparing(Book::getAuthor, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER)
            .thenComparingInt(Book::getId)),

    YEAR(Comparator.comparingInt(Book::getPublicationYear)
            .thenComparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER)
            .thenComparingInt(Book::getId));

    private final Comparator<Book> comparator;

    SortField(Comparator<Book> comparator) {
        this.comparator = comparator;
    }

    public Comparator<Book> comparator() {
        return comparator;
    }
}