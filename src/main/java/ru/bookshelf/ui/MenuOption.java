package ru.bookshelf.ui;

import java.util.Optional;

/**
 * Пункты главного меню. Номер пункта вычисляется из порядка констант,
 * поэтому нумерация в меню и разбор ввода не могут разойтись.
 */
public enum MenuOption {
    ADD("Добавить книгу"),
    SHOW_ALL("Показать все книги"),
    SEARCH("Найти книгу"),
    EDIT("Редактировать книгу"),
    DELETE("Удалить книгу"),
    SORT("Отсортировать книги"),
    EXIT("Выход");

    private final String label;

    MenuOption(String label) {
        this.label = label;
    }

    /** Номер пункта, как его видит пользователь (с единицы). */
    public int number() {
        return ordinal() + 1;
    }

    public String label() {
        return label;
    }

    /**
     * Разбирает ввод пользователя. Пустую строку, текст и числа вне диапазона
     * превращает в {@link Optional#empty()}.
     */
    public static Optional<MenuOption> fromInput(String text) {
        try {
            int number = Integer.parseInt(text.strip());
            MenuOption[] all = values();
            if (number >= 1 && number <= all.length) {
                return Optional.of(all[number - 1]);
            }
            return Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}