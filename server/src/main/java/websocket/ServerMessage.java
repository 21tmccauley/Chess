package websocket;

/**
 * Base class for all messages sent from server to client.
 * All specific message types inherit from this class.
 */
public abstract class ServerMessage {
    public final ServerMessageType serverMessageType;

    protected ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }
}
