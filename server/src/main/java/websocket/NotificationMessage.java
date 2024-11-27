package websocket;

/**
 * Message sent to clients to notify them of game events
 */
public class NotificationMessage extends ServerMessage {
    public final String message;

    public NotificationMessage(String message) {
        super(ServerMessageType.NOTIFICATION);
        this.message = message;
    }
}
