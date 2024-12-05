package client;

import websocket.messages.ServerMessage;

public interface MessageHandler {
    void handleMessage(ServerMessage message);
}