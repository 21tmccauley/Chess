package chess;

import java.util.Collection;

public interface MoveStrategy {
    Collection<ChessMove> calculateMoves(ChessPiece piece, ChessBoard board, ChessPosition position, GameState gameState);
}
