package websocket.messages;

/**
 * Represents an error message from the server.
 * This is sent when a client makes an invalid request or when something goes wrong.
 */

public class ErrorMessage extends ServerMessage {
    private final String errorMessage;

    public ErrorMessage(String errorMessage) {
        super(ServerMessageType.ERROR);
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
