package ru.bookshelf;

import ru.bookshelf.repository.BookRepository;
import ru.bookshelf.repository.impl.InMemoryBookRepository;
import ru.bookshelf.service.BookService;
import ru.bookshelf.service.impl.BookServiceImpl;
import ru.bookshelf.ui.ConsoleInput;
import ru.bookshelf.ui.ConsoleMenu;
import ru.bookshelf.ui.ConsoleOutput;
import ru.bookshelf.validation.BookValidator;
import ru.bookshelf.validation.impl.BookValidatorImpl;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Точка входа. Только создаёт объекты и связывает их через конструкторы:
 * хранилище → сервис → меню. Глобального состояния нет.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        ConsoleOutput output = new ConsoleOutput(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        BookRepository repository = new InMemoryBookRepository();
        BookValidator validator = new BookValidatorImpl();
        BookService service = new BookServiceImpl(repository, validator);

        // Единственный Scanner на System.in живёт внутри ConsoleInput и закрывается здесь
        try (ConsoleInput input = new ConsoleInput(System.in, output)) {
            new ConsoleMenu(service, input, output).run();
        }
    }
}