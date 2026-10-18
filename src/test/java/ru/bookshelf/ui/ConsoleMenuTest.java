package ru.bookshelf.ui;

import org.junit.jupiter.api.Test;
import ru.bookshelf.repository.impl.InMemoryBookRepository;
import ru.bookshelf.service.BookService;
import ru.bookshelf.service.impl.BookServiceImpl;
import ru.bookshelf.validation.impl.BookValidatorImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Сквозные тесты: настоящие меню, ввод, вывод, сервис, валидатор и хранилище,
 * на входе заранее заданный сценарий ввода, на выходе проверяется текст консоли.
 */
class ConsoleMenuTest {

    private static final String EXIT = "7";

    /** Прогоняет меню на сценарии ввода (каждая строка = Enter) и возвращает весь вывод. */
    private static String run(String... lines) {
        String script = String.join("\n", lines) + "\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ConsoleOutput output = new ConsoleOutput(new PrintStream(bytes, true, StandardCharsets.UTF_8));
        BookService service = new BookServiceImpl(new InMemoryBookRepository(), new BookValidatorImpl());
        try (ConsoleInput input = new ConsoleInput(
                new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)), output)) {
            new ConsoleMenu(service, input, output).run();
        }
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private static int count(String text, String part) {
        int count = 0;
        for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + part.length())) {
            count++;
        }
        return count;
    }

    private static String after(String text, String marker) {
        int index = text.lastIndexOf(marker);
        assertTrue(index >= 0, "в выводе нет: " + marker);
        return text.substring(index);
    }

    // ---------- меню ----------

    @Test
    void showsMenuAndExits() {
        String out = run(EXIT);

        assertTrue(out.contains("1. Добавить книгу"));
        assertTrue(out.contains("6. Отсортировать книги"));
        assertTrue(out.contains("7. Выход"));
        assertTrue(out.contains("До свидания!"));
    }

    @Test
    void invalidMenuInputShowsErrorAndKeepsRunning() {
        String out = run("", "   ", "abc", "0", "8", "1.5", EXIT);

        assertEquals(6, count(out, "Нет такого пункта меню"));
        assertTrue(out.contains("До свидания!"));
    }

    @Test
    void endOfInputEndsProgramGracefully() {
        String out = run("2"); // нет пункта «Выход»: поток просто заканчивается

        assertTrue(out.contains("Каталог пуст"));
        assertTrue(out.contains("Ввод завершён"));
    }

    // ---------- пустой каталог ----------

    @Test
    void emptyCatalogGivesInformativeMessages() {
        String out = run("2", "6", "1", "3", "1", "война", EXIT);

        assertTrue(out.contains("Каталог пуст. Добавьте первую книгу через пункт меню 1."));
        assertTrue(out.contains("Каталог пуст: сортировать нечего."));
        assertTrue(out.contains("Ничего не найдено по запросу «война»."));
    }

    // ---------- добавление ----------

    @Test
    void addsBookAndShowsItInTable() {
        String out = run("1", "Война и мир", "Лев Толстой", "1869", "Роман", "", "2", EXIT);

        assertTrue(out.contains("[OK] Книга добавлена (ID 1):"));
        String table = after(out, "Все книги (книг: 1):");
        assertTrue(table.contains("Война и мир"));
        assertTrue(table.contains("Лев Толстой"));
        assertTrue(table.contains("1869"));
        assertTrue(table.contains("Не прочитана"), "статус по умолчанию — UNREAD");
    }

    @Test
    void nonNumericAndEmptyYearAreReprompted() {
        String out = run("1", "Дюна", "Фрэнк Герберт", "тысяча", "", "1965", "Фантастика", "2", "2", EXIT);

        assertTrue(out.contains("«тысяча» не подходит: нужно целое число."));
        assertTrue(out.contains("Значение не введено. Введите целое число."));
        assertTrue(out.contains("[OK] Книга добавлена (ID 1):"));
        assertTrue(after(out, "Все книги (книг: 1):").contains("Читаю"));
    }

    @Test
    void invalidStatusChoiceIsReprompted() {
        String out = run("1", "Дюна", "Фрэнк Герберт", "1965", "Фантастика", "9", "abc", "3", EXIT);

        assertEquals(2, count(out, "Введите номер от 1 до 3."));
        assertTrue(out.contains("[OK] Книга добавлена (ID 1):"));
        assertTrue(out.contains("Прочитана"));
    }

    @Test
    void validationErrorsListEveryFieldAndReturnToMenuWithoutSaving() {
        String out = run("1", "", "  ", "3000", "", "", "2", EXIT);

        assertTrue(out.contains("Название: не может быть пустым"));
        assertTrue(out.contains("Автор: не может быть пустым"));
        assertTrue(out.contains("Жанр: не может быть пустым"));
        assertTrue(out.contains("Год публикации: должен быть от 1 до"));
        assertFalse(out.contains("Книга добавлена"));
        assertTrue(after(out, "Год публикации: должен быть от 1 до").contains("Каталог пуст"));
    }

    // ---------- поиск ----------

    private static final String[] TWO_BOOKS = {
            "1", "Война и мир", "Лев Толстой", "1869", "Роман", "",
            "1", "Мастер и Маргарита", "Михаил Булгаков", "1967", "Фантастика", "2"};

    private static String[] withScript(String... tail) {
        String[] all = new String[TWO_BOOKS.length + tail.length];
        System.arraycopy(TWO_BOOKS, 0, all, 0, TWO_BOOKS.length);
        System.arraycopy(tail, 0, all, TWO_BOOKS.length, tail.length);
        return all;
    }

    @Test
    void searchIsCaseInsensitiveAndPartial() {
        String out = run(withScript("3", "1", "ВОЙНА", EXIT));

        String result = after(out, "Результаты поиска (книг: 1):");
        assertTrue(result.contains("Война и мир"));
        assertFalse(result.contains("Маргарита"));
    }

    @Test
    void searchByAuthorAndGenre() {
        String out = run(withScript("3", "2", "булгак", "3", "3", "РОМАН", EXIT));

        assertTrue(out.contains("Результаты поиска (книг: 1):"));
        assertEquals(2, count(out, "Результаты поиска (книг: 1):"));
    }

    @Test
    void searchWithNoMatchesAndBlankQueryReturnToMenu() {
        String out = run(withScript("3", "1", "хоббит", "3", "1", "   ", EXIT));

        assertTrue(out.contains("Ничего не найдено по запросу «хоббит»."));
        assertTrue(out.contains("Поисковый запрос: не может быть пустым"));
        assertTrue(out.contains("До свидания!"));
    }

    // ---------- сортировка ----------

    @Test
    void sortByYearOrdersRowsNumerically() {
        String out = run("1", "Б", "А", "2005", "Ж", "", "1", "А", "Б", "987", "Ж", "", "1", "В", "В", "1999", "Ж", "",
                "6", "3", EXIT);

        String sorted = after(out, "Отсортировано по году публикации (книг: 3):");
        int y987 = sorted.indexOf("987");
        int y1999 = sorted.indexOf("1999");
        int y2005 = sorted.indexOf("2005");
        assertTrue(y987 > 0 && y987 < y1999 && y1999 < y2005, sorted);
    }

    @Test
    void sortDoesNotChangeOrderOfShowAll() {
        String out = run("1", "Б", "А", "2005", "Ж", "", "1", "А", "Б", "1990", "Ж", "",
                "6", "1", "2", EXIT);

        String all = after(out, "Все книги (книг: 2):");
        assertTrue(all.indexOf("2005") < all.indexOf("1990"), "порядок добавления должен сохраниться");
    }

    // ---------- редактирование ----------

    @Test
    void editShowsCurrentDataAndKeepsBlankFields() {
        String out = run("1", "Дюна", "Фрэнк Герберт", "1965", "Фантастика", "",
                "4", "1",            // выбираем книгу
                "",                  // название: оставить
                "Ф. Герберт",        // автор: изменить
                "",                  // год: оставить
                "",                  // жанр: оставить
                "3",                 // статус: Прочитана
                "2", EXIT);

        assertTrue(out.contains("Текущие данные:"));
        String updated = after(out, "[OK] Книга обновлена:");
        assertTrue(updated.contains("Дюна"));
        assertTrue(updated.contains("Ф. Герберт"));
        assertTrue(updated.contains("1965"));
        assertTrue(updated.contains("Прочитана"));
        assertTrue(after(out, "Все книги (книг: 1):").contains("Ф. Герберт"));
    }

    @Test
    void editWithInvalidDataLeavesBookUnchanged() {
        String out = run("1", "Дюна", "Фрэнк Герберт", "1965", "Фантастика", "",
                "4", "1", "", "", "3000", "", "",
                "2", EXIT);

        assertTrue(out.contains("Год публикации: должен быть от 1 до"));
        assertFalse(out.contains("Книга обновлена"));
        assertTrue(after(out, "Все книги (книг: 1):").contains("1965"));
    }

    @Test
    void editOfMissingBookShowsErrorAndReturnsToMenu() {
        String out = run("4", "abc", "99", EXIT);

        assertTrue(out.contains("«abc» не подходит: нужно целое число."));
        assertTrue(out.contains("Книга с ID 99 не найдена"));
        assertTrue(out.contains("До свидания!"));
    }

    @Test
    void editWithNonPositiveIdShowsValidationError() {
        String out = run("4", "0", EXIT);

        assertTrue(out.contains("ID: должен быть положительным числом (получено 0)"));
    }

    // ---------- удаление ----------

    @Test
    void deleteAsksForConfirmationAndCanBeCancelled() {
        String out = run("1", "Дюна", "Фрэнк Герберт", "1965", "Фантастика", "",
                "5", "1", "н",
                "5", "1", "",           // пустой ответ = «нет»
                "2", EXIT);

        assertTrue(out.contains("Будет удалена книга:"));
        assertEquals(2, count(out, "Удаление отменено."));
        assertFalse(out.contains("удалена."));
        assertTrue(after(out, "Все книги (книг: 1):").contains("Дюна"));
    }

    @Test
    void deleteWithConfirmationRemovesBook() {
        String out = run("1", "Дюна", "Фрэнк Герберт", "1965", "Фантастика", "",
                "5", "1", "maybe", "д",
                "2", EXIT);

        assertTrue(out.contains("Ответьте «д» или «н»."));
        assertTrue(out.contains("[OK] Книга с ID 1 удалена."));
        assertTrue(after(out, "[OK] Книга с ID 1 удалена.").contains("Каталог пуст"));
    }

    @Test
    void deleteOfMissingBookShowsErrorWithoutAskingConfirmation() {
        String out = run("5", "42", EXIT);

        assertTrue(out.contains("Книга с ID 42 не найдена"));
        assertFalse(out.contains("Удалить эту книгу?"));
    }

    @Test
    void idsAreNotReusedAfterDeletion() {
        String out = run("1", "А", "А", "2000", "Ж", "",
                "5", "1", "д",
                "1", "Б", "Б", "2001", "Ж", "", EXIT);

        assertTrue(out.contains("[OK] Книга добавлена (ID 2):"));
    }

    // ---------- отображение ----------

    @Test
    void longValuesAreTruncatedInTableButFullInDetails() {
        String longTitle = "Очень длинное название книги, которое не помещается в колонку таблицы целиком";
        String out = run("1", longTitle, "Автор", "2000", "Жанр", "", "2", EXIT);

        String table = after(out, "Все книги (книг: 1):");
        assertTrue(table.contains("..."), "длинное название усекается");
        assertFalse(table.contains(longTitle));
        assertTrue(out.contains("Название:       " + longTitle), "в карточке название целиком");
    }
}