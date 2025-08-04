package bot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.flywaydb.core.Flyway;
import storage.DatabaseConfig;

public class App {
    public static void main(String[] args) {
        // Run Flyway migrations
        DatabaseConfig config = new DatabaseConfig();
        Flyway flyway = Flyway.configure()
                .dataSource(config.getDbUrl(), config.getDbUser(), config.getDbPassword())
                .locations("classpath:migrations")
                .load();
        flyway.migrate();

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new PomodoroBot());
            System.out.println("Pomodoro Bot запущен!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
