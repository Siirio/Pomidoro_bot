package config;

public class ConfigReaderIEnvironment implements ConfigReader {
    @Override
    public Config read() {
        String token = System.getenv("BOT_TOKEN");
        String dbUrl = System.getenv("DB_URL");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");

        System.err.println("DEBUG: ConfigReaderIEnvironment - BOT_TOKEN: " + (token != null ? "SET" : "NOT SET"));
        System.err.println("DEBUG: ConfigReaderIEnvironment - DB_URL: " + (dbUrl != null ? "SET" : "NOT SET"));
        System.err.println("DEBUG: ConfigReaderIEnvironment - DB_USER: " + (dbUser != null ? "SET" : "NOT SET"));
        System.err.println("DEBUG: ConfigReaderIEnvironment - DB_PASSWORD: " + (dbPassword != null ? "SET" : "NOT SET"));

        if (token == null || token.isEmpty()) {
            System.err.println("❌ ERROR: BOT_TOKEN environment variable is not set!");
            System.err.println("Please set the BOT_TOKEN environment variable with your Telegram bot token");
            System.exit(1);
        }

        if (dbUrl == null || dbUrl.isEmpty()) {
            System.err.println("❌ ERROR: DB_URL environment variable is not set!");
            System.exit(1);
        }
        if (dbUser == null || dbUser.isEmpty()) {
            System.err.println("❌ ERROR: DB_USER environment variable is not set!");
            System.exit(1);
        }
        if (dbPassword == null || dbPassword.isEmpty()) {
            System.err.println("❌ ERROR: DB_PASSWORD environment variable is not set!");
            System.exit(1);
        }

        System.err.println("✅ Environment variables loaded successfully from ConfigReader");
        return new Config(dbUrl, dbUser, dbPassword, token);
    }
}