package websocket;

import chess.ChessGame;

/**
 * Message sent to clients when they need to update their game state
 */
public class LoadGameMessage extends ServerMessage {
    public final ChessGame game;

    public LoadGameMessage(ChessGame game) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
    }
}