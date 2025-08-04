package bot;

import model.UserSession;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import service.StatisticsService;
import storage.DataBaseUserDataRepository;
import storage.DatabaseConfig;
import timer.PomodoroTimer;
import ui.KeyboardFactory;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

public class PomodoroBot extends TelegramLongPollingBot {
    private final Map<Long, UserSession> sessions = new HashMap<>();
    private final StatisticsService statisticsService;

    public PomodoroBot() {
        DatabaseConfig config = new DatabaseConfig();
        this.statisticsService = new DataBaseUserDataRepository(
            config.getDbUrl(),
            config.getDbUser(),
            config.getDbPassword()
        );
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
                    """, KeyboardFactory.mainMenu());
            case "/start_pomo" -> {
                int durationSeconds = 25 * 60; // default 25 min
                String[] parts = text.trim().split("\\s+");
                if (parts.length > 1) {
                    try {
                        durationSeconds = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException ignored) {}
                }
                new PomodoroTimer(session, statisticsService).startWork(durationSeconds);
            }
            case "/stop" -> session.stopTimer();
            case "/stats" -> {
                String stats = statisticsService.getStats(userId);
                session.sendMessage(stats, KeyboardFactory.mainMenu());
            }
            case "/achievements" -> {
                String achievements = statisticsService.getAchievements(userId);
                session.sendMessage(achievements, KeyboardFactory.mainMenu());
            }
            case "/export_stats" -> {
                byte[] csv = statisticsService.exportStats(userId);
                if (csv != null && csv.length > 0) {
                    try {
                        SendDocument doc = new SendDocument();
                        doc.setChatId(String.valueOf(chatId));
                        doc.setDocument(new InputFile(new ByteArrayInputStream(csv), "stats_" + userId + ".csv"));
                        execute(doc);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    session.sendMessage("Нет данных для экспорта.", KeyboardFactory.mainMenu());
                }
            }
            default -> session.sendMessage("❓ Неизвестная команда. Используй /start чтобы увидеть список команд.");
        }
    }
}