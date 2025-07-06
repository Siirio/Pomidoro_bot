package timer;

import model.UserSession;
import storage.UserDataService;
import ui.KeyboardFactory;

public class PomodoroTimer {
    private final UserSession session;
    private final UserDataService dataService;

    public PomodoroTimer(UserSession session, UserDataService dataService) {
        this.session = session;
        this.dataService = dataService;
    }

    public void startWork() {
        session.sendMessage("🍅 Помидорка началась! Работай 25 минут.", KeyboardFactory.mainMenu());
        TimerThread timer = new TimerThread(session, 25 * 60, () -> {
            dataService.recordWork(session.getUserId());
            session.sendMessage("⏰ Время закончилось! Пора отдыхать.", KeyboardFactory.mainMenu());
            startRest();
        });
        session.setTimer(timer);
    }

    private void startRest() {
        session.sendMessage("☕ Начинается 5-минутный перерыв.", KeyboardFactory.mainMenu());
        TimerThread timer = new TimerThread(session, 5 * 60, () -> {
            dataService.recordRest(session.getUserId());
            session.sendMessage("🔔 Перерыв закончился! Готов к следующему раунду?", KeyboardFactory.mainMenu());
        });
        session.setTimer(timer);
    }
}