package service;

public interface StatisticsService {

    void recordPomodoro(long userId);
    void recordPomodoro(long userId, int durationSeconds);
    String getStats(long userId);
    String getAchievements(long userId);
    byte[] exportStats(long userId); // CSV as byte array
    void recordRest(long userId);
}