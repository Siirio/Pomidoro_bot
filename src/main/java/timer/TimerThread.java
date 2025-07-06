package timer;

import model.UserSession;

public class TimerThread extends Thread {
    private final UserSession session;
    private final int seconds;
    private final Runnable onFinish;

    public TimerThread(UserSession session, int seconds, Runnable onFinish) {
        this.session = session;
        this.seconds = seconds;
        this.onFinish = onFinish;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(seconds * 1000L);
            onFinish.run();
        } catch (InterruptedException ignored) {
        }
    }
}