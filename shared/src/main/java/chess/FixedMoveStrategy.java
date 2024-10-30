package chess;

import java.util.ArrayList;
import java.util.Collection;

import static chess.ChessUtils.isValidPosition;

public class FixedMoveStrategy implements MoveStrategy {
    private final int[][] moveOffsets;
    private final boolean isKing;

    public FixedMoveStrategy(int[][] moveOffsets, boolean isKing) {
        this.moveOffsets = moveOffsets;
        this.isKing = isKing;
    }

    @Override
    public Collection<ChessMove> calculateMoves(ChessPiece piece, ChessBoard board, ChessPosition position, GameState gameState) {
        Collection<ChessMove> moves = new ArrayList<>();
        int currentRow = position.getRow();
        int currentColumn = position.getColumn();

        for (int[] offset : moveOffsets) {
            int newRow = currentRow + offset[0];
            int newColumn = currentColumn + offset[1];

            if (isValidPosition(newRow, newColumn)) {
                ChessPosition newPosition = new ChessPosition(newRow, newColumn);
                ChessPiece targetPiece = board.getPiece(newPosition);

                if (targetPiece == null || targetPiece.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(position, newPosition, null));
                }
            }
        }

        return moves;
    }
}