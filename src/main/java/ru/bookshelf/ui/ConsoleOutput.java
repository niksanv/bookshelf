package ru.bookshelf.ui;

import ru.bookshelf.model.Book;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Весь вывод в консоль: сообщения, меню, карточка книги и таблица книг.
 * Не содержит бизнес-логики и не читает ввод.
 */
public class ConsoleOutput {

    private static final int MAX_TITLE_WIDTH = 40;
    private static final int MAX_AUTHOR_WIDTH = 25;
    private static final int MAX_GENRE_WIDTH = 20;
    private static final String ELLIPSIS = "...";

    private final PrintStream out;

    public ConsoleOutput(PrintStream out) {
        this.out = Objects.requireNonNull(out, "out");
    }

    public void printWelcome() {
        out.println("==========================================");
        out.println("   BookShelf: менеджер личной библиотеки");
        out.println("==========================================");
    }

    public void printMenu(MenuOption[] options) {
        out.println();
        out.println("Главное меню:");
        for (MenuOption option : options) {
            out.println("  " + option.number() + ". " + option.label());
        }
    }

    public void printInfo(String message) {
        out.println(message);
    }

    public void printSuccess(String message) {
        out.println("[OK] " + message);
    }

    public void printError(String message) {
        out.println("[Ошибка] " + message);
    }

    public void printValidationErrors(List<String> errors) {
        out.println("[Ошибка] Некорректные данные:");
        for (String error : errors) {
            out.println("  - " + error);
        }
    }

    /** Приглашение к вводу: без перевода строки, с принудительным сбросом буфера. */
    public void printPrompt(String prompt) {
        out.print(prompt);
        out.flush();
    }

    /** Полная карточка книги: значения выводятся целиком, без усечения. */
    public void printBookDetails(Book book) {
        printField("ID", String.valueOf(book.getId()));
        printField("Название", book.getTitle());
        printField("Автор", book.getAuthor());
        printField("Год публикации", String.valueOf(book.getPublicationYear()));
        printField("Жанр", book.getGenre());
        printField("Статус", Labels.status(book.getReadingStatus()));
    }

    /**
     * Таблица книг. Ширина колонок подбирается по содержимому; слишком длинные
     * название, автор и жанр усекаются многоточием (полные значения видны в карточке).
     */
    public void printBookTable(List<Book> books) {
        int idWidth = width("ID", books, book -> String.valueOf(book.getId()), Integer.MAX_VALUE);
        int titleWidth = width("Название", books, Book::getTitle, MAX_TITLE_WIDTH);
        int authorWidth = width("Автор", books, Book::getAuthor, MAX_AUTHOR_WIDTH);
        int yearWidth = width("Год", books, book -> String.valueOf(book.getPublicationYear()), Integer.MAX_VALUE);
        int genreWidth = width("Жанр", books, Book::getGenre, MAX_GENRE_WIDTH);
        int statusWidth = width("Статус", books, book -> Labels.status(book.getReadingStatus()), Integer.MAX_VALUE);

        String rowFormat = " %" + idWidth + "s | %-" + titleWidth + "s | %-" + authorWidth
                + "s | %" + yearWidth + "s | %-" + genreWidth + "s | %s%n";

        out.printf(rowFormat, "ID", "Название", "Автор", "Год", "Жанр", "Статус");
        out.println("-".repeat(idWidth + 2) + "+" + "-".repeat(titleWidth + 2) + "+"
                + "-".repeat(authorWidth + 2) + "+" + "-".repeat(yearWidth + 2) + "+"
                + "-".repeat(genreWidth + 2) + "+" + "-".repeat(statusWidth + 1));
        for (Book book : books) {
            out.printf(rowFormat,
                    book.getId(),
                    fit(book.getTitle(), titleWidth),
                    fit(book.getAuthor(), authorWidth),
                    book.getPublicationYear(),
                    fit(book.getGenre(), genreWidth),
                    Labels.status(book.getReadingStatus()));
        }
    }

    private void printField(String name, String value) {
        out.printf("  %-16s%s%n", name + ":", value);
    }

    private static int width(String header, List<Book> books, Function<Book, String> value, int maxWidth) {
        int width = header.length();
        for (Book book : books) {
            width = Math.max(width, value.apply(book).length());
        }
        return Math.min(width, maxWidth);
    }

    private static String fit(String text, int width) {
        if (text.length() <= width) {
            return text;
        }
        return text.substring(0, width - ELLIPSIS.length()) + ELLIPSIS;
    }
}