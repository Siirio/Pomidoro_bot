package repository;

public interface UserDataRepository {
    void recordPomodoro(long userId, int durationSeconds);
    void recordRest(long userId);
    String getStats(long userId);
    String getAchievements(long userId);
    byte[] exportStats(long userId);
}
