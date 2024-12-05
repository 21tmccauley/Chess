package client;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {
    private Session session;
    private final MessageHandler messageHandler;
    private final Gson gson;

    public WebSocketFacade(String url, MessageHandler messageHandler) throws Exception {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.messageHandler = messageHandler;
            this.gson = new Gson();

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new javax.websocket.MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
                    messageHandler.handleMessage(serverMessage);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new Exception("Failed to connect to server: " + ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        // Required by Endpoint but can be empty
    }

    public void sendCommand(UserGameCommand command) throws Exception {
        try {
            String jsonCommand = gson.toJson(command);
            this.session.getBasicRemote().sendText(jsonCommand);
        } catch (IOException ex) {
            throw new Exception("Failed to send command: " + ex.getMessage());
        }
    }

    public void disconnect() throws Exception {
        try {
            if (session != null) {
                session.close();
            }
        } catch (IOException ex) {
            throw new Exception("Failed to disconnect: " + ex.getMessage());
        }
    }
}