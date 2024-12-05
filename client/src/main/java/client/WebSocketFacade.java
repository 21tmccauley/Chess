package client;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ErrorMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Facade that handles WebSocket communication for the chess client.
 * This provides a simpler interface for WebSocket operations and handles
 * the complexities of message serialization and connection management.
 */
public class WebSocketFacade extends Endpoint {
    private Session session;
    private final MessageHandler messageHandler;
    private final Gson gson;

    /**
     * Creates a new WebSocket connection to the chess server.
     *
     * @param serverUrl Base URL of the server (will be converted to WebSocket URL)
     * @param messageHandler Handler for incoming server messages
     * @throws Exception if connection fails
     */
    public WebSocketFacade(String serverUrl, MessageHandler messageHandler) throws Exception {
        try {
            // Setup basic requirements
            this.messageHandler = messageHandler;
            this.gson = new Gson();

            // Convert HTTP URL to WebSocket URL and create connection
            String wsUrl = serverUrl.replace("http", "ws");
            URI socketURI = new URI(wsUrl + "/ws");

            // Connect to the server
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            // Set up message handling
            this.session.addMessageHandler(new javax.websocket.MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    handleServerMessage(message);
                }
            });

        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new Exception("Failed to connect to server: " + ex.getMessage());
        }
    }

    /**
     * Required by Endpoint class, called when connection is established
     */
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        System.out.println("Debug: WebSocket connection opened");
    }

    /**
     * Sends a command to the chess server
     * @param command The command to send
     * @throws Exception if sending fails
     */
    public void sendCommand(UserGameCommand command) throws Exception {
        try {
            if (session != null && session.isOpen()) {
                String jsonCommand = gson.toJson(command);
                session.getBasicRemote().sendText(jsonCommand);
            } else {
                throw new Exception("WebSocket connection is not open");
            }
        } catch (IOException ex) {
            throw new Exception("Failed to send command: " + ex.getMessage());
        }
    }

    /**
     * Closes the WebSocket connection
     */
    public void close() {
        try {
            if (session != null) {
                session.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing WebSocket connection: " + e.getMessage());
        }
    }

    /**
     * Handles incoming messages from the server
     */
    private void handleServerMessage(String messageJson) {
        try {
            // First parse as base message to get the type
            ServerMessage baseMessage = gson.fromJson(messageJson, ServerMessage.class);

            // Then parse to specific message type based on the message type
            switch (baseMessage.getServerMessageType()) {
                case LOAD_GAME -> {
                    LoadGameMessage gameMessage = gson.fromJson(messageJson, LoadGameMessage.class);
                    messageHandler.handleGameUpdate(gameMessage);
                }
                case NOTIFICATION -> {
                    NotificationMessage notification = gson.fromJson(messageJson, NotificationMessage.class);
                    messageHandler.handleNotification(notification);
                }
                case ERROR -> {
                    ErrorMessage error = gson.fromJson(messageJson, ErrorMessage.class);
                    messageHandler.handleError(error.getErrorMessage());
                }
            }
        } catch (Exception e) {
            messageHandler.handleError("Error processing message: " + e.getMessage());
        }
    }
}

