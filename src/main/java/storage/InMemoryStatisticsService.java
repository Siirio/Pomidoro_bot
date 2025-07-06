package storage;

import service.StatisticsService;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class InMemoryStatisticsService implements StatisticsService {
    private final Map<Long, Integer> userStats = new HashMap<>();

    @Override
    public void recordPomodoro(long userId) {
        userStats.put(userId, userStats.getOrDefault(userId, 0) + 1);
    }

    @Override
    public String getStats(long userId) {
        int count = userStats.getOrDefault(userId, 0);
        return "📊 Вы завершили " + count + " помидорок!";
    }

    @Override
    public String getAchievements(long userId) {
        int count = userStats.getOrDefault(userId, 0);
        if (count >= 10) return "🏆 Вы — мастер продуктивности!";
        if (count >= 5) return "🎯 Хорошее начало!";
        if (count > 0) return "🍅 Продолжай в том же духе!";
        return "⏳ Вы еще не начали работать.";
    }

    @Override
    public byte[] exportStats(long userId) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(out)) {

            writer.println("user_id,sessions_completed");
            writer.println(userId + "," + userStats.getOrDefault(userId, 0));
            writer.flush();
            return out.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}