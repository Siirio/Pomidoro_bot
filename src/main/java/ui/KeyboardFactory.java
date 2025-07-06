package ui;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

public class KeyboardFactory {
    public static ReplyKeyboardMarkup mainMenu() {
        KeyboardRow row1 = new KeyboardRow(List.of(
                new KeyboardButton("/start_pomo"),
                new KeyboardButton("/stop")
        ));
        KeyboardRow row2 = new KeyboardRow(List.of(
                new KeyboardButton("/stats"),
                new KeyboardButton("/achievements")
        ));
        KeyboardRow row3 = new KeyboardRow(List.of(
                new KeyboardButton("/export_stats") // alone in last row
        ));

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(List.of(row1, row2, row3));
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);
        return keyboard;
    }
}