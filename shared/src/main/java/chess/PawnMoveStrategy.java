package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMoveStrategy implements MoveStrategy {
    @Override
    public Collection<ChessMove> calculateMoves(ChessPiece piece, ChessBoard board, ChessPosition position, GameState gameState) {
        Collection<ChessMove> moves = new ArrayList<>();
        // ... existing pawn move logic ...

        // En passant logic
        ChessPosition enPassantTarget = gameState.getEnPassantTarget();
        if (enPassantTarget != null) {
            int rowDiff = enPassantTarget.getRow() - position.getRow();
            int colDiff = enPassantTarget.getColumn() - position.getColumn();
            if (Math.abs(rowDiff) == 1 && Math.abs(colDiff) == 1) {
                if ((piece.getTeamColor() == ChessGame.TeamColor.WHITE && rowDiff > 0) ||
                        (piece.getTeamColor() == ChessGame.TeamColor.BLACK && rowDiff < 0)) {
                    moves.add(new ChessMove(position, enPassantTarget, null));
                }
            }
        }

        return moves;
    }
}