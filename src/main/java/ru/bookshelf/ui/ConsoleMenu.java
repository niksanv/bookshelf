package ru.bookshelf.ui;

import ru.bookshelf.exception.BookShelfException;
import ru.bookshelf.exception.ValidationException;
import ru.bookshelf.model.Book;
import ru.bookshelf.model.BookInput;
import ru.bookshelf.model.ReadingStatus;
import ru.bookshelf.model.SearchField;
import ru.bookshelf.model.SortField;
import ru.bookshelf.service.BookService;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Главное меню: цикл выбора пунктов и сценарии диалога с пользователем.
 * <p>
 * Класс только собирает ввод, вызывает {@link BookService} и показывает результат.
 * Бизнес-правил и обращений к репозиторию здесь нет. Любая ошибка операции
 * показывается сообщением, после чего пользователь возвращается в меню.
 */
public class ConsoleMenu {

    private static final List<ReadingStatus> STATUSES = List.of(ReadingStatus.values());
    private static final List<SearchField> SEARCH_FIELDS = List.of(SearchField.values());
    private static final List<SortField> SORT_FIELDS = List.of(SortField.values());

    private final BookService service;
    private final ConsoleInput input;
    private final ConsoleOutput output;

    public ConsoleMenu(BookService service, ConsoleInput input, ConsoleOutput output) {
        this.service = Objects.requireNonNull(service, "service");
        this.input = Objects.requireNonNull(input, "input");
        this.output = Objects.requireNonNull(output, "output");
    }

    /** Запускает цикл меню; возвращает управление после пункта «Выход» или конца ввода. */
    public void run() {
        output.printWelcome();
        try {
            boolean running = true;
            while (running) {
                output.printMenu(MenuOption.values());
                Optional<MenuOption> option = MenuOption.fromInput(input.readLine("Выберите пункт меню: "));
                if (option.isPresent()) {
                    running = execute(option.get());
                } else {
                    output.printError("Нет такого пункта меню. Введите число от 1 до "
                            + MenuOption.values().length + ".");
                }
            }
            output.printInfo("До свидания!");
        } catch (InputEndedException e) {
            output.printInfo("");
            output.printInfo("Ввод завершён. До свидания!");
        }
    }

    /** Выполняет пункт меню. Возвращает {@code false}, если нужно выйти из программы. */
    private boolean execute(MenuOption option) {
        try {
            switch (option) {
                case ADD -> addBook();
                case SHOW_ALL -> showAllBooks();
                case SEARCH -> searchBooks();
                case EDIT -> editBook();
                case DELETE -> deleteBook();
                case SORT -> sortBooks();
                case EXIT -> { }
            }
        } catch (ValidationException e) {
            output.printValidationErrors(e.getErrors());
        } catch (BookShelfException e) {
            output.printError(e.getMessage());
        }
        return option != MenuOption.EXIT;
    }

    private void addBook() {
        output.printInfo("--- Добавление книги ---");
        String title = input.readLine("Название: ");
        String author = input.readLine("Автор: ");
        int year = input.readInt("Год публикации: ");
        String genre = input.readLine("Жанр: ");
        ReadingStatus status = input.readChoice("Статус прочтения:", STATUSES, Labels::status, ReadingStatus.UNREAD);

        Book book = service.addBook(new BookInput(title, author, year, genre, status));
        output.printSuccess("Книга добавлена (ID " + book.getId() + "):");
        output.printBookDetails(book);
    }

    private void showAllBooks() {
        showBooks(service.getAllBooks(),
                "Все книги",
                "Каталог пуст. Добавьте первую книгу через пункт меню "
                        + MenuOption.ADD.number() + ".");
    }

    private void searchBooks() {
        output.printInfo("--- Поиск книги ---");
        SearchField field = input.readChoice("Где искать:", SEARCH_FIELDS, Labels::search, null);
        String query = input.readLine("Введите запрос (часть слова, регистр не важен): ");

        List<Book> found = service.searchBooks(field, query);
        showBooks(found,
                "Результаты поиска",
                "Ничего не найдено по запросу «" + query.strip() + "».");
    }

    private void editBook() {
        output.printInfo("--- Редактирование книги ---");
        int id = input.readInt("ID книги (список с ID: пункт меню " + MenuOption.SHOW_ALL.number() + "): ");
        Book current = service.getBookById(id);

        output.printInfo("Текущие данные:");
        output.printBookDetails(current);
        output.printInfo("Введите новые значения. Пустая строка: оставить без изменений.");

        String title = keepIfBlank(input.readLine("Название [" + current.getTitle() + "]: "), current.getTitle());
        String author = keepIfBlank(input.readLine("Автор [" + current.getAuthor() + "]: "), current.getAuthor());
        int year = input.readOptionalInt("Год публикации [" + current.getPublicationYear() + "]: ")
                .orElse(current.getPublicationYear());
        String genre = keepIfBlank(input.readLine("Жанр [" + current.getGenre() + "]: "), current.getGenre());
        ReadingStatus status = input.readChoice("Статус прочтения:", STATUSES, Labels::status,
                current.getReadingStatus());

        Book updated = service.updateBook(id, new BookInput(title, author, year, genre, status));
        output.printSuccess("Книга обновлена:");
        output.printBookDetails(updated);
    }

    private void deleteBook() {
        output.printInfo("--- Удаление книги ---");
        int id = input.readInt("ID книги (список с ID: пункт меню " + MenuOption.SHOW_ALL.number() + "): ");
        Book book = service.getBookById(id);

        output.printInfo("Будет удалена книга:");
        output.printBookDetails(book);
        if (input.confirm("Удалить эту книгу?")) {
            service.deleteBook(id);
            output.printSuccess("Книга с ID " + id + " удалена.");
        } else {
            output.printInfo("Удаление отменено.");
        }
    }

    private void sortBooks() {
        output.printInfo("--- Сортировка книг ---");
        SortField field = input.readChoice("Сортировать:", SORT_FIELDS, Labels::sort, null);

        showBooks(service.getAllBooksSorted(field),
                "Отсортировано " + Labels.sort(field).toLowerCase(Locale.ROOT),
                "Каталог пуст: сортировать нечего.");
    }

    /** Показывает таблицу с заголовком и количеством либо сообщение, если список пуст. */
    private void showBooks(List<Book> books, String header, String emptyMessage) {
        if (books.isEmpty()) {
            output.printInfo(emptyMessage);
            return;
        }
        output.printInfo(header + " (книг: " + books.size() + "):");
        output.printBookTable(books);
    }

    private static String keepIfBlank(String entered, String current) {
        return entered.isBlank() ? current : entered;
    }
}