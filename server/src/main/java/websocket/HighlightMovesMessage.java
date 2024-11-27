package websocket;

import chess.ChessMove;
import java.util.Collection;

/**
 * Message sent to clients to show valid moves for a selected piece
 */
public class HighlightMovesMessage extends ServerMessage {
    public final Collection<ChessMove> moves;

    public HighlightMovesMessage(Collection<ChessMove> moves) {
        super(ServerMessageType.HIGHLIGHT_MOVES);
        this.moves = moves;
    }
}
