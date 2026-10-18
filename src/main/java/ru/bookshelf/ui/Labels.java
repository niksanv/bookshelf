package ru.bookshelf.ui;

import ru.bookshelf.model.ReadingStatus;
import ru.bookshelf.model.SearchField;
import ru.bookshelf.model.SortField;

/**
 * Русские подписи для значений модели. Модель ничего не знает о языке интерфейса,
 * а исчерпывающий switch не даст забыть подпись для новой константы.
 */
public final class Labels {

    private Labels() {
    }

    public static String status(ReadingStatus status) {
        return switch (status) {
            case UNREAD -> "Не прочитана";
            case READING -> "Читаю";
            case READ -> "Прочитана";
        };
    }

    public static String search(SearchField field) {
        return switch (field) {
            case TITLE -> "По названию";
            case AUTHOR -> "По автору";
            case GENRE -> "По жанру";
        };
    }

    public static String sort(SortField field) {
        return switch (field) {
            case TITLE -> "По названию";
            case AUTHOR -> "По автору";
            case YEAR -> "По году публикации";
        };
    }
}