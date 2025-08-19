package repository;

import config.Config;
import service.StatisticsService;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataBaseUserDataRepository implements UserDataRepository, StatisticsService {
    private static final Logger LOGGER = Logger.getLogger(DataBaseUserDataRepository.class.getName());

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public DataBaseUserDataRepository(Config config) {
        this.dbUrl = config.dbUrl();
        this.dbUser = config.dbUser();
        this.dbPassword = config.dbPassword();
    }

    @Override
    public void recordPomodoro(long userId) {
        // Default 25 minutes if duration is not provided
        recordPomodoro(userId, 25 * 60);
    }

    @Override
    public void recordPomodoro(long userId, int durationSeconds) {
        LocalDate today = LocalDate.now();
        int workMinutes = durationSeconds / 60;

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO user_sessions (chat_id, date, work_cycles, total_minutes) " +
                            "VALUES (?, ?, 1, ?) " +
                            "ON CONFLICT (chat_id, date) DO UPDATE " +
                            "SET work_cycles = user_sessions.work_cycles + 1, " +
                            "total_minutes = user_sessions.total_minutes + ?")) {

                stmt.setLong(1, userId);
                stmt.setDate(2, Date.valueOf(today));
                stmt.setInt(3, workMinutes);
                stmt.setInt(4, workMinutes);
                stmt.executeUpdate();
            }

            // Check for achievements
            checkAndRecordAchievements(userId, conn);

            conn.commit();
            LOGGER.info("Successfully recorded pomodoro for user " + userId);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error recording pomodoro: " + e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error during rollback: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection: " + e.getMessage(), e);
                }
            }
        }
    }

    private void checkAndRecordAchievements(long userId, Connection conn) {
        try (PreparedStatement stmt = conn.prepareStatement(
                     "SELECT SUM(work_cycles) as total_cycles FROM user_sessions WHERE chat_id = ?");
             ) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int totalCycles = rs.getInt("total_cycles");

                    if (totalCycles >= 1) {
                        recordAchievement(userId, "🍅", "Первый помидор!", conn);
                    }
                    if (totalCycles >= 5) {
                        recordAchievement(userId, "🎯", "Хорошее начало!", conn);
                    }
                    if (totalCycles >= 10) {
                        recordAchievement(userId, "🏆", "Мастер продуктивности!", conn);
                    }
                    if (totalCycles >= 25) {
                        recordAchievement(userId, "⭐", "Звезда продуктивности!", conn);
                    }
                    if (totalCycles >= 50) {
                        recordAchievement(userId, "🔥", "Огненная продуктивность!", conn);
                    }
                    if (totalCycles >= 100) {
                        recordAchievement(userId, "💯", "Сотня помидоров!", conn);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking achievements: " + e.getMessage(), e);
            // Do not fail the main transaction due to achievements; log and continue
        }
    }

    private void recordAchievement(long userId, String emoji, String title, Connection conn) {
        try (PreparedStatement checkStmt = conn.prepareStatement(
                     "SELECT id FROM user_achievements WHERE chat_id = ? AND title = ?")) {
            checkStmt.setLong(1, userId);
            checkStmt.setString(2, title);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(
                            "INSERT INTO user_achievements (chat_id, emoji, title, achieved_on) VALUES (?, ?, ?, ?)")) {
                        insertStmt.setLong(1, userId);
                        insertStmt.setString(2, emoji);
                        insertStmt.setString(3, title);
                        insertStmt.setDate(4, Date.valueOf(LocalDate.now()));
                        insertStmt.executeUpdate();
                        LOGGER.info("New achievement recorded for user " + userId + ": " + title);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error recording achievement: " + e.getMessage(), e);
            // Do not fail the main transaction due to achievements
        }
    }

    @Override
    public void recordRest(long userId) {
        LocalDate today = LocalDate.now();
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            int updatedRows;
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE user_sessions SET total_minutes = total_minutes + 5 WHERE chat_id = ? AND date = ?")) {
                stmt.setLong(1, userId);
                stmt.setDate(2, Date.valueOf(today));
                updatedRows = stmt.executeUpdate();
            }

            if (updatedRows == 0) {
                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO user_sessions (chat_id, date, work_cycles, total_minutes) VALUES (?, ?, 0, 5)")) {
                    insertStmt.setLong(1, userId);
                    insertStmt.setDate(2, Date.valueOf(today));
                    insertStmt.executeUpdate();
                }
            }

            conn.commit();
            LOGGER.info("Successfully recorded rest for user " + userId);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error recording rest: " + e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error during rollback: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection: " + e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public String getStats(long userId) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT SUM(work_cycles) as total_cycles, SUM(total_minutes) as total_minutes FROM user_sessions WHERE chat_id = ?")) {

            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int totalCycles = rs.getInt("total_cycles");
                    int totalMinutes = rs.getInt("total_minutes");
                    int hours = totalMinutes / 60;
                    int minutes = totalMinutes % 60;

                    return String.format("📊 Статистика:\n" +
                            "🍅 Всего помидоров: %d\n" +
                            "⏱ Общее время работы: %d ч %d мин", totalCycles, hours, minutes);
                } else {
                    return "📊 У вас пока нет статистики. Начните работу с помидорами!";
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting stats: " + e.getMessage(), e);
            return "❌ Не удалось получить статистику. Попробуйте позже.";
        }
    }

    @Override
    public String getAchievements(long userId) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT emoji, title FROM user_achievements WHERE chat_id = ? ORDER BY achieved_on")) {

            stmt.setLong(1, userId);
            List<String> achievements = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String emoji = rs.getString("emoji");
                    String title = rs.getString("title");
                    achievements.add(emoji + " " + title);
                }
            }

            if (achievements.isEmpty()) {
                return "🏆 У вас пока нет достижений. Начните работу с помидорами!";
            } else {
                StringBuilder result = new StringBuilder("🏆 Ваши достижения:\n");
                for (String achievement : achievements) {
                    result.append(achievement).append("\n");
                }
                return result.toString();
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting achievements: " + e.getMessage(), e);
            return "❌ Не удалось получить достижения. Попробуйте позже.";
        }
    }

    @Override
    public byte[] exportStats(long userId) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT date, work_cycles, total_minutes FROM user_sessions WHERE chat_id = ? ORDER BY date")) {

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(out, true);

            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                writer.println("date,work_cycles,total_minutes");
                while (rs.next()) {
                    Date date = rs.getDate("date");
                    int workCycles = rs.getInt("work_cycles");
                    int totalMinutes = rs.getInt("total_minutes");
                    writer.println(date + "," + workCycles + "," + totalMinutes);
                }
            }

            writer.flush();
            LOGGER.info("Successfully exported stats for user " + userId);
            return out.toByteArray();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error exporting stats: " + e.getMessage(), e);
            return new byte[0];
        }
    }

    private Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to database: " + e.getMessage(), e);
            throw new SQLException("Failed to connect to database. Please check your database configuration.", e);
        }
    }

    // Legacy close helpers no longer required thanks to try-with-resources
}