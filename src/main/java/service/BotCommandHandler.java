package service;

import model.UserSession;

public interface BotCommandHandler {
    void handle(UserSession session);
}
