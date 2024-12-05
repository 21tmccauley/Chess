package client;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import websocket.commands.ConnectCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class ConnectionManager implements MessageHandler {
    private final String serverUrl;
    private final client.MessageHandler clientMessageHandler;
    private final Gson gson;
    private WebSocketFacade webSocket;
    private String authToken;

    public ConnectionManager(String serverUrl, client.MessageHandler clientMessageHandler) {
        this.serverUrl = serverUrl;
        this.clientMessageHandler = clientMessageHandler;
        this.gson = new Gson();
    }

    /**
     * Updates the authentication token used for game commands
     * This token is included in all commands sent to the server
     */
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
        // If we have an active connection, close it since the auth state changed
        if (webSocket != null) {
            webSocket.close();
            webSocket = null;
        }
    }

    /**
     * Gets the current authentication token
     */
    public String getAuthToken() {
        return authToken;
    }

    public void connectToGame(int gameId) throws Exception {
        // Close any existing connection
        if (webSocket != null) {
            webSocket.close();
        }

        // Create new WebSocket connection
        webSocket = new WebSocketFacade(serverUrl, this);

        // Send connect command
        ConnectCommand connectCommand = new ConnectCommand(authToken, gameId);
        webSocket.sendCommand(connectCommand);
    }

    public void sendCommand(UserGameCommand command) throws Exception {
        if (webSocket != null) {
            webSocket.sendCommand(command);
        } else {
            throw new Exception("No active WebSocket connection");
        }
    }

    public void closeConnection() {
        if (webSocket != null) {
            webSocket.close();
            webSocket = null;
        }
    }

    // Implement MessageHandler interface methods
    @Override
    public void handleGameUpdate(LoadGameMessage message) {
        clientMessageHandler.handleGameUpdate(message);
    }

    @Override
    public void handleNotification(NotificationMessage message) {
        clientMessageHandler.handleNotification(message);
    }

    @Override
    public void handleError(String message) {
        clientMessageHandler.handleError(message);
    }
}