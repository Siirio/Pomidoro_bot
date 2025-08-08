package timer;

import model.UserSession;

public class TimerThread extends Thread {
    public enum Type { WORK, REST }
    private final UserSession session;
    private final int seconds;
    private final Runnable onFinish;
    private final Type type;

    public TimerThread(UserSession session, int seconds, Runnable onFinish, Type type) {
        this.session = session;
        this.seconds = seconds;
        this.onFinish = onFinish;
        this.type = type;
    }

    public int getSeconds() { return seconds; }
    public Type getType() { return type; }

    @Override
    public void run() {
        try {
            Thread.sleep(seconds * 1000L);
            onFinish.run();
        } catch (InterruptedException ignored) {
        }
    }
}