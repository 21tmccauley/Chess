package websocket;

import chess.ChessMove;
import chess.ChessPosition;

/**
 * Represents a command sent from the client to the server.
 * Contains all possible fields needed for any command type.
 */
public class UserGameCommand {
    public CommandType commandType;
    public String authToken;
    public Integer gameID;
    public ChessMove move;        // Used for MAKE_MOVE
    public ChessPosition piece;   // Used for HIGHLIGHT_MOVES
}
