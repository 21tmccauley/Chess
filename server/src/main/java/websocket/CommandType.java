package websocket;

/**
 * Defines all possible commands that can be sent from client to server
 */
public enum CommandType {
    CONNECT,
    MAKE_MOVE,
    LEAVE,
    RESIGN,
    HIGHLIGHT_MOVES
}
