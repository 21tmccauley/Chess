package client;

import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public interface MessageHandler {
    void handleGameUpdate(LoadGameMessage message);
    void handleNotification(NotificationMessage message);
    void handleError(String errorMessage);
}