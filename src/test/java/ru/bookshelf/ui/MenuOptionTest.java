package ru.bookshelf.ui;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuOptionTest {

    @Test
    void numbersStartFromOneInDeclarationOrder() {
        assertEquals(1, MenuOption.ADD.number());
        assertEquals(7, MenuOption.EXIT.number());
    }

    @Test
    void parsesEveryValidNumber() {
        for (MenuOption option : MenuOption.values()) {
            assertEquals(Optional.of(option), MenuOption.fromInput(String.valueOf(option.number())));
        }
    }

    @Test
    void ignoresSurroundingWhitespace() {
        assertEquals(Optional.of(MenuOption.SEARCH), MenuOption.fromInput("  3 \t"));
    }

    @Test
    void rejectsEverythingElse() {
        for (String text : new String[]{"", "   ", "0", "8", "-1", "abc", "1.5", "2 3", "99999999999999999999"}) {
            assertTrue(MenuOption.fromInput(text).isEmpty(), "должно быть отвергнуто: '" + text + "'");
        }
    }
}