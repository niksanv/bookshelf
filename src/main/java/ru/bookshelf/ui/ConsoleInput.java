package ru.bookshelf.ui;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;

/**
 * Весь ввод с консоли. Владеет единственным {@link Scanner} на поток ввода
 * (создаётся в конструкторе, закрывается в {@link #close()}).
 * <p>
 * Ошибки ввода (пустая строка, текст вместо числа, номер вне диапазона) обрабатываются
 * здесь повторным запросом, не доходя до бизнес-логики. Правила предметной области
 * (длины, диапазон года и т. д.) проверяет сервис.
 */
public class ConsoleInput implements AutoCloseable {

    private static final Set<String> YES_ANSWERS = Set.of("д", "да", "y", "yes");
    private static final Set<String> NO_ANSWERS = Set.of("н", "нет", "n", "no");

    private final Scanner scanner;
    private final ConsoleOutput output;

    public ConsoleInput(InputStream in, ConsoleOutput output) {
        this.scanner = new Scanner(Objects.requireNonNull(in, "in"), StandardCharsets.UTF_8);
        this.output = Objects.requireNonNull(output, "output");
    }

    /**
     * Показывает приглашение и возвращает введённую строку как есть (возможно, пустую).
     *
     * @throws InputEndedException если поток ввода закончился
     */
    public String readLine(String prompt) {
        output.printPrompt(prompt);
        try {
            return scanner.nextLine();
        } catch (NoSuchElementException e) {
            throw new InputEndedException();
        }
    }

    /** Читает целое число, повторяя запрос при пустом вводе и тексте вместо числа. */
    public int readInt(String prompt) {
        while (true) {
            OptionalInt value = readOptionalInt(prompt);
            if (value.isPresent()) {
                return value.getAsInt();
            }
            output.printError("Значение не введено. Введите целое число.");
        }
    }

    /**
     * Читает целое число; пустая строка означает «не вводить» и даёт пустой результат.
     * Текст вместо числа вызывает повторный запрос.
     */
    public OptionalInt readOptionalInt(String prompt) {
        while (true) {
            String line = readLine(prompt).strip();
            if (line.isEmpty()) {
                return OptionalInt.empty();
            }
            try {
                return OptionalInt.of(Integer.parseInt(line));
            } catch (NumberFormatException e) {
                output.printError("«" + line + "» не подходит: нужно целое число.");
            }
        }
    }

    /**
     * Показывает нумерованный список и возвращает выбранный элемент.
     *
     * @param defaultValue значение для пустого ввода; если {@code null}, пустой ввод не допускается
     */
    public <T> T readChoice(String title, List<T> options, Function<T, String> label, T defaultValue) {
        output.printInfo(title);
        for (int i = 0; i < options.size(); i++) {
            output.printInfo("  " + (i + 1) + ". " + label.apply(options.get(i)));
        }
        String prompt = defaultValue == null
                ? "Ваш выбор: "
                : "Ваш выбор [Enter: " + label.apply(defaultValue) + "]: ";

        while (true) {
            String line = readLine(prompt).strip();
            if (line.isEmpty() && defaultValue != null) {
                return defaultValue;
            }
            try {
                int number = Integer.parseInt(line);
                if (number >= 1 && number <= options.size()) {
                    return options.get(number - 1);
                }
            } catch (NumberFormatException ignored) {
                // обрабатывается общим сообщением ниже
            }
            output.printError("Введите номер от 1 до " + options.size() + ".");
        }
    }

    /**
     * Запрашивает подтверждение. Пустой ввод означает «нет» (безопасный вариант);
     * непонятный ответ вызывает повторный запрос.
     */
    public boolean confirm(String question) {
        while (true) {
            String answer = readLine(question + " (д/н) [н]: ").strip().toLowerCase(Locale.ROOT);
            if (answer.isEmpty() || NO_ANSWERS.contains(answer)) {
                return false;
            }
            if (YES_ANSWERS.contains(answer)) {
                return true;
            }
            output.printError("Ответьте «д» или «н».");
        }
    }

    @Override
    public void close() {
        scanner.close();
    }
}