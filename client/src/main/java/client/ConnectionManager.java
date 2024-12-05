package client;

import com.google.gson.Gson;
import websocket.commands.ConnectCommand;
import websocket.commands.UserGameCommand;
import client.WebSocketFacade;  // Update this import path based on where your WebSocketFacade is
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

public class ConnectionManager implements MessageHandler {
    private final String serverUrl;
    private final MessageHandler clientMessageHandler;
    private WebSocketFacade webSocket;
    private String authToken;
    private final Gson gson;

    public ConnectionManager(String serverUrl, MessageHandler clientMessageHandler) {
        this.serverUrl = serverUrl;
        this.clientMessageHandler = clientMessageHandler;
        this.gson = new Gson();
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
        // Close existing connection if auth state changes
        if (webSocket != null) {
            closeConnection();
            webSocket = null;
        }
    }

    public String getAuthToken() {
        return authToken;
    }

    public void connectToGame(int gameId) throws Exception {
        try {
            // Close any existing connection
            if (webSocket != null) {
                closeConnection();
            }

            // Create new WebSocket connection
            webSocket = new WebSocketFacade(serverUrl, this);

            // Send connect command
            ConnectCommand connectCommand = new ConnectCommand(authToken, gameId);
            sendCommand(connectCommand);

        } catch (Exception e) {
            throw new Exception("Failed to connect to game: " + e.getMessage());
        }
    }

    public void sendCommand(UserGameCommand command) throws Exception {
        if (webSocket == null) {
            throw new Exception("No active WebSocket connection");
        }
        webSocket.sendCommand(command);
    }

    public void closeConnection() {
        try {
            if (webSocket != null) {
                webSocket.disconnect();
                webSocket = null;
            }
        } catch (Exception e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    @Override
    public void handleMessage(ServerMessage message) {
        // Simply forward the message to the client message handler
        clientMessageHandler.handleMessage(message);
    }
}