package websocket.messages;


import chess.ChessGame;

/**
 * Represents a message from the server containing the current state of a chess game.
 * This message is sent whenever the game state needs to be updated on a client.
 */
public class LoadGameMessage extends ServerMessage {
    private final ChessGame game;

    public LoadGameMessage(ChessGame game) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
    }
    public ChessGame getGame() {
        return game;
    }
}
