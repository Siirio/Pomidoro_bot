package bot;

import model.UserSession;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import storage.UserDataService;
import timer.PomodoroTimer;
import ui.KeyboardFactory;

import java.util.HashMap;
import java.util.Map;

public class PomodoroBot extends TelegramLongPollingBot {
    private final Map<Long, UserSession> sessions = new HashMap<>();
    private final UserDataService userDataService;

    public PomodoroBot() {
        this.userDataService = new UserDataService(this); // 👈 pass AbsSender to service
    }

    @Override
    public String getBotUsername() {
        return "YourPomodoroBotUsername"; // <- Replace with your bot name
    }

    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN");
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        var msg = update.getMessage();
        var userId = msg.getFrom().getId();
        var chatId = msg.getChatId();
        var text = msg.getText();

        sessions.putIfAbsent(userId, new UserSession(userId, chatId, this));
        var session = sessions.get(userId);

        switch (text) {
            case "/start" -> session.sendMessage("""
                    👋 Привет! Я — Pomodoro-бот.
                    Вот что я умею:

                    ▶️ /start_pomo — начать рабочий таймер
                    ⏹️ /stop — остановить текущий таймер
                    📊 /stats — статистика
                    🏆 /achievements — достижения
                    📁 /export_stats — экспорт в CSV
                    """, ui.KeyboardFactory.mainMenu()); // ✅ include keyboard
            case "/start_pomo" -> new PomodoroTimer(session, userDataService).startWork();
            case "/stop" -> session.stopTimer();
            case "/stats" -> userDataService.sendStats(chatId);
            case "/achievements" -> userDataService.sendAchievements(chatId);
            case "/export_stats" -> userDataService.exportStats(chatId);
            default -> session.sendMessage("❓ Неизвестная команда. Используй /start чтобы увидеть список команд.");
        }
    }
}