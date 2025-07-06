package storage;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.*;
import java.time.LocalDate;

public class UserDataService {

    private final AbsSender sender;

    public UserDataService(AbsSender sender) {
        this.sender = sender;
    }

    public void recordWork(long userId) {
        appendStats(userId, true);
        checkAchievements(userId);
    }

    public void recordRest(long userId) {
        appendStats(userId, false);
    }

    public void sendStats(long chatId) {
        String filename = "stats_" + chatId + ".csv";
        int workCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("work")) workCount++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String response = "📊 Вы завершили " + workCount + " помидорок!";
        try {
            sender.execute(SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text(response)
                    .replyMarkup(ui.KeyboardFactory.mainMenu())
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendAchievements(long chatId) {
        String filename = "stats_" + chatId + ".csv";
        int workCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("work")) workCount++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String achievement;
        if (workCount >= 10) {
            achievement = "🏆 Вы — мастер продуктивности!";
        } else if (workCount >= 5) {
            achievement = "🎯 Хорошее начало!";
        } else if (workCount > 0) {
            achievement = "🍅 Продолжай в том же духе!";
        } else {
            achievement = "⏳ Вы еще не начали работать.";
        }

        try {
            sender.execute(SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text(achievement)
                    .replyMarkup(ui.KeyboardFactory.mainMenu())
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exportStats(long chatId) {
        String filename = "stats_" + chatId + ".csv";
        File file = new File(filename);
        if (file.exists()) {
            try {
                SendDocument doc = new SendDocument();
                doc.setChatId(String.valueOf(chatId));
                doc.setDocument(new InputFile(file));
                sender.execute(doc); // ✅ Correct sender
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Файл не найден: " + filename);
        }
    }

    private void appendStats(long userId, boolean isWork) {
        String filename = "stats_" + userId + ".csv";
        try (FileWriter fw = new FileWriter(filename, true);
             PrintWriter pw = new PrintWriter(fw)) {
            String line = LocalDate.now() + "," + (isWork ? "work" : "rest");
            pw.println(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkAchievements(long userId) {
        // Optional: track milestones like 5 or 10 pomodoros
    }
}