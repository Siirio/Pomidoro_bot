package bot;

import model.UserSession;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import service.StatisticsService;
import config.Config;
import config.ConfigReader;
import config.ConfigReaderIEnvironment;
import repository.DataBaseUserDataRepository;
import timer.PomodoroTimer;
import ui.KeyboardFactory;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

public class PomodoroBot extends TelegramLongPollingBot {
    private static final String CMD_START = "/start";
    private static final String CMD_START_POMO = "/start_pomo";
    private static final String CMD_STOP = "/stop";
    private static final String CMD_STATS = "/stats";
    private static final String CMD_ACHIEVEMENTS = "/achievements";
    private static final String CMD_EXPORT = "/export_stats";

    private static final int DEFAULT_POMODORO_MINUTES = 25;

    private final Map<Long, UserSession> userIdToSession = new HashMap<>();
    private final StatisticsService statisticsService;
    private final String botToken;

    public PomodoroBot() {
        ConfigReader configReader = new ConfigReaderIEnvironment();
        Config config = configReader.read();
        this.statisticsService = new DataBaseUserDataRepository(config);
        this.botToken = config.botToken();
    }

    @Override
    public String getBotUsername() {
        return "Pom I Doro"; // <- Replace with your bot name
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        var msg = update.getMessage();
        var userId = msg.getFrom().getId();
        var chatId = msg.getChatId();
        var text = msg.getText();
        var normalizedText = text == null ? "" : text.trim();

        userIdToSession.putIfAbsent(userId, new UserSession(userId, chatId, this, statisticsService));
        var session = userIdToSession.get(userId);

        // handle commands with arguments and aliases first
        if (normalizedText.startsWith(CMD_START_POMO) || normalizedText.startsWith("/star_pomo")) {
            int minutes = DEFAULT_POMODORO_MINUTES;
            String[] parts = normalizedText.split("\\s+");
            if (parts.length > 1) {
                try {
                    minutes = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) { }
            }
            int durationSeconds = Math.max(1, minutes) * 60;
            new PomodoroTimer(session, statisticsService).startWork(durationSeconds);
            return;
        }

        switch (normalizedText) {
            case CMD_START -> session.sendMessage("""
                    👋 Привет! Я — Pomodoro-бот.
                    Вот что я умею:

                    ▶️ /start_pomo — начать рабочий таймер (можно указать минуты: /start_pomo 25)
                    ⏹️ /stop — остановить текущий таймер
                    📊 /stats — статистика
                    🏆 /achievements — достижения
                    📁 /export_stats — экспорт в CSV
                    """, KeyboardFactory.mainMenu());
            case CMD_STOP -> session.stopTimer();
            case CMD_STATS -> {
                String stats = statisticsService.getStats(chatId);
                session.sendMessage(stats, KeyboardFactory.mainMenu());
            }
            case CMD_ACHIEVEMENTS -> {
                String achievements = statisticsService.getAchievements(chatId);
                session.sendMessage(achievements, KeyboardFactory.mainMenu());
            }
            case CMD_EXPORT -> {
                byte[] csv = statisticsService.exportStats(chatId);
                if (csv != null && csv.length > 0) {
                    try {
                        SendDocument doc = new SendDocument();
                        doc.setChatId(String.valueOf(chatId));
                        doc.setDocument(new InputFile(new ByteArrayInputStream(csv), "stats_" + chatId + ".csv"));
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