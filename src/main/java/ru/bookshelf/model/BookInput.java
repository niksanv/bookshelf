package ru.bookshelf.model;

/**
 * Данные книги, введённые пользователем (без ID, который назначает система).
 * <p>
 * Текстовые поля сразу очищаются от пробелов по краям, поэтому «   » считается пустой строкой.
 * {@code null} сохраняется как есть — его отвергает валидатор с понятным сообщением.
 */
public record BookInput(String title,
                        String author,
                        int publicationYear,
                        String genre,
                        ReadingStatus readingStatus) {

    public BookInput {
        title = strip(title);
        author = strip(author);
        genre = strip(genre);
    }

    private static String strip(String value) {
        return value == null ? null : value.strip();
    }
}