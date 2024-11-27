package websocket;

/**
 * Message sent to clients when an error occurs
 */
public class ErrorMessage extends ServerMessage {
    public final String errorMessage;

    public ErrorMessage(String message) {
        super(ServerMessageType.ERROR);
        this.errorMessage = "Error: " + message;
    }
}
