package timer;

import model.UserSession;
import service.StatisticsService;
import ui.KeyboardFactory;

public class PomodoroTimer {
    private final UserSession session;
    private final StatisticsService statisticsService;

    public PomodoroTimer(UserSession session, StatisticsService statisticsService) {
        this.session = session;
        this.statisticsService = statisticsService;
    }

    public void startWork() {
        startWork(25 * 60); // default 25 min
    }

    public void startWork(int durationSeconds) {
        int minutes = durationSeconds / 60;
        session.sendMessage("🍅 Помидорка началась! Работай " + minutes + " минут.", KeyboardFactory.mainMenu());
        TimerThread timer = new TimerThread(session, durationSeconds, () -> {
            statisticsService.recordPomodoro(session.getChatId(), durationSeconds);
            session.sendMessage("⏰ Время закончилось! Пора отдыхать.", KeyboardFactory.mainMenu());
            startRest();
        }, TimerThread.Type.WORK);
        session.setTimer(timer);
    }

    private void startRest() {
        session.sendMessage("☕ Начинается 5-минутный перерыв.", KeyboardFactory.mainMenu());
        TimerThread timer = new TimerThread(session, 5 * 60, () -> {
            statisticsService.recordRest(session.getChatId());
            session.sendMessage("🔔 Перерыв закончился! Готов к следующему раунду?", KeyboardFactory.mainMenu());
        }, TimerThread.Type.REST);
        session.setTimer(timer);
    }
}