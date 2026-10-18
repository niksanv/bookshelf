package ru.bookshelf.model;

import java.util.Objects;

/**
 * Книга в личной библиотеке.
 * <p>
 * Класс неизменяемый: «изменение» книги выполняется созданием нового объекта
 * с тем же ID. Идентичность книги определяется только её ID.
 * <p>
 * Бизнес-проверки содержимого (длина строк, диапазон года и т. д.) здесь не выполняются —
 * они находятся в слое валидации. Конструктор лишь защищает модель от заведомо
 * некорректного состояния: неположительного ID и null в обязательных полях.
 */
public final class Book {

    private final int id;
    private final String title;
    private final String author;
    private final int publicationYear;
    private final String genre;
    private final ReadingStatus readingStatus;

    public Book(int id,
                String title,
                String author,
                int publicationYear,
                String genre,
                ReadingStatus readingStatus) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID книги должен быть положительным: " + id);
        }
        this.id = id;
        this.title = Objects.requireNonNull(title, "title");
        this.author = Objects.requireNonNull(author, "author");
        this.publicationYear = publicationYear;
        this.genre = Objects.requireNonNull(genre, "genre");
        this.readingStatus = Objects.requireNonNull(readingStatus, "readingStatus");
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public String getGenre() {
        return genre;
    }

    public ReadingStatus getReadingStatus() {
        return readingStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Book other)) {
            return false;
        }
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    /** Только для отладки и сообщений тестов; пользовательский вывод формирует слой UI. */
    @Override
    public String toString() {
        return "Book{id=" + id
                + ", title='" + title + '\''
                + ", author='" + author + '\''
                + ", publicationYear=" + publicationYear
                + ", genre='" + genre + '\''
                + ", readingStatus=" + readingStatus + '}';
    }
}