package websocket;

/**
 * Defines all possible types of messages that can be sent from server to client
 */
public enum ServerMessageType {
    LOAD_GAME,
    ERROR,
    NOTIFICATION,
    HIGHLIGHT_MOVES
}