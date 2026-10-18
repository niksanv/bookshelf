package ru.bookshelf.validation;

/**
 * Числовые границы правил валидации. Единственное место, где их нужно менять.
 */
public final class ValidationRules {

    public static final int MAX_TITLE_LENGTH = 200;
    public static final int MAX_AUTHOR_LENGTH = 100;
    public static final int MAX_GENRE_LENGTH = 50;

    /** Нижняя граница года публикации. Верхняя — текущий год, вычисляется при проверке. */
    public static final int MIN_PUBLICATION_YEAR = 1;

    private ValidationRules() {
    }
}