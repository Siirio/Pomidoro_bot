package bot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.flywaydb.core.Flyway;
import config.Config;
import config.ConfigReader;
import config.ConfigReaderIEnvironment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) {
        // Run Flyway migrations
        ConfigReader configReader = new ConfigReaderIEnvironment();
        Config config = configReader.read();

        try (Connection conn = DriverManager.getConnection(
                config.dbUrl(), config.dbUser(), config.dbPassword())) {
            System.out.println("✅ DB connection OK: " + conn.getMetaData().getURL());
        } catch (SQLException e) {
            System.err.println("❌ Cannot connect to DB with provided settings.");
            System.err.println("DB_URL=" + config.dbUrl());
            System.err.println("DB_USER=" + config.dbUser());
            e.printStackTrace();
            System.exit(1);
        }


        Flyway flyway = Flyway.configure()
                .dataSource(config.dbUrl(), config.dbUser(), config.dbPassword())
                .locations("classpath:migrations")
                .load();
        // Repair schema history to fix checksum mismatches in dev
        try {
            flyway.repair();
        } catch (Exception ignored) {}
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
