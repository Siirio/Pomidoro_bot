package service;

public interface StatisticsService {
    void recordPomodoro(long userId);
    String getStats(long userId);
    String getAchievements(long userId);
    byte[] exportStats(long userId); // CSV as byte array
}