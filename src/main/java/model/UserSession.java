package model;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import timer.TimerThread;
import service.StatisticsService;

public class UserSession {
    private final long userId;
    private final long chatId;
    private TimerThread activeTimer;
    private final AbsSender sender; // 👈 Add this
    private final StatisticsService statisticsService;
    private long workSessionStartTime = 0;
    private int workSessionPlannedSeconds = 0;

    public UserSession(long userId, long chatId, org.telegram.telegrambots.meta.bots.AbsSender sender, service.StatisticsService statisticsService) {
        this.userId = userId;
        this.chatId = chatId;
        this.sender = sender;
        this.statisticsService = statisticsService;
    }

    public long getUserId() { return userId; }
    public long getChatId() { return chatId; }

    public void setTimer(TimerThread timer) {
        if (activeTimer != null) activeTimer.interrupt();
        this.activeTimer = timer;
        // Track work session start time and planned duration if this is a work timer
        if (timer.getType() == TimerThread.Type.WORK) {
            this.workSessionStartTime = System.currentTimeMillis();
            this.workSessionPlannedSeconds = timer.getSeconds();
        } else {
            this.workSessionStartTime = 0;
            this.workSessionPlannedSeconds = 0;
        }
        timer.start();
    }

    public void stopTimer() {
        if (activeTimer != null) {
            // If this was a work session, record partial if >=30s
            if (activeTimer.getType() == TimerThread.Type.WORK && workSessionStartTime > 0) {
                long elapsed = (System.currentTimeMillis() - workSessionStartTime) / 1000;
                if (elapsed >= 30) {
                    // Record partial session
                    statisticsService.recordPomodoro(chatId, (int) elapsed);
                    sendMessage("⏹️ Таймер остановлен. Засчитано " + (elapsed/60) + " минут.", ui.KeyboardFactory.mainMenu());
                } else {
                    sendMessage("⏹️ Таймер остановлен. Слишком короткая сессия для зачёта.", ui.KeyboardFactory.mainMenu());
                }
            } else {
                sendMessage("⏹️ Таймер остановлен.", ui.KeyboardFactory.mainMenu());
            }
            activeTimer.interrupt();
            activeTimer = null;
            workSessionStartTime = 0;
            workSessionPlannedSeconds = 0;
        } else {
            sendMessage("Нет активного таймера.", ui.KeyboardFactory.mainMenu());
        }
    }

    public void sendMessage(String text, ReplyKeyboardMarkup keyboard) {
        try {
            SendMessage msg = SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text(text)
                    .replyMarkup(keyboard)
                    .build();
            sender.execute(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Перегрузка
    public void sendMessage(String text) {
        sendMessage(text, null);
    }
}
