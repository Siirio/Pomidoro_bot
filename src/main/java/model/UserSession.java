package model;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import timer.TimerThread;

public class UserSession {
    private final long userId;
    private final long chatId;
    private TimerThread activeTimer;
    private final AbsSender sender; // 👈 Add this

    public UserSession(long userId, long chatId, AbsSender sender) {
        this.userId = userId;
        this.chatId = chatId;
        this.sender = sender;
    }

    public long getUserId() { return userId; }
    public long getChatId() { return chatId; }

    public void setTimer(TimerThread timer) {
        if (activeTimer != null) activeTimer.interrupt();
        this.activeTimer = timer;
        timer.start();
    }

    public void stopTimer() {
        if (activeTimer != null) {
            activeTimer.interrupt();
            activeTimer = null;
        }
        sendMessage("⏹️ Таймер остановлен.", ui.KeyboardFactory.mainMenu());
    }

    public void sendMessage(String text, ReplyKeyboardMarkup keyboard) {
        try {
            SendMessage msg = SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text(text)
                    .replyMarkup(keyboard)
                    .build();
            sender.execute(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Перегрузка
    public void sendMessage(String text) {
        sendMessage(text, null);
    }
}
