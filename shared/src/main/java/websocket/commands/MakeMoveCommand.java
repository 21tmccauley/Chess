package websocket.commands;

import chess.ChessMove;
/**
 * Represents a command from a client to make a move in a chess game.
 * This command includes all the standard UserGameCommand fields (commandType, authToken, gameID)
 * plus a move field containing the details of the chess move to be made.
 */


public class MakeMoveCommand extends UserGameCommand {
    private final ChessMove move;

    public MakeMoveCommand(String authToken, Integer gameID, ChessMove move) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.move = move;
    }

    public ChessMove getMove() {
        return move;
    }

}
