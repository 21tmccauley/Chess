package websocket.messages;

/**
 * Represents a notification message from the server.
 * This is used to inform clients about game events like:
 * - Players joining/leaving
 * - Moves being made
 * - Players resigning
 * - Check/checkmate situations
 */
public class NotificationMessage extends ServerMessage {
    private final String message;  // Must be named 'message' according to requirements

    public NotificationMessage(String message) {
        super(ServerMessageType.NOTIFICATION);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}