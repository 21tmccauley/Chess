package chess;

import java.util.ArrayList;
import java.util.Collection;

public class SlidingMoveStrategy implements MoveStrategy {
    private final int[][] directions;

    public SlidingMoveStrategy(int[][] directions) {
        this.directions = directions;
    }

    @Override
    public Collection<ChessMove> calculateMoves(ChessPiece piece, ChessBoard board, ChessPosition position, GameState gameState) {
        Collection<ChessMove> moves = new ArrayList<>();
        int currentRow = position.getRow();
        int currentColumn = position.getColumn();

        for (int[] direction : directions) {
            int row = currentRow;
            int column = currentColumn;

            while (true) {
                row += direction[0];
                column += direction[1];

                if (!isValidPosition(row, column)) {
                    break;
                }

                ChessPosition newPosition = new ChessPosition(row, column);
                ChessPiece targetPiece = board.getPiece(newPosition);

                if (targetPiece == null) {
                    // Empty square, add move
                    moves.add(new ChessMove(position, newPosition, null));
                } else if (targetPiece.getTeamColor() != piece.getTeamColor()) {
                    // Opponent's piece, add capture move and stop in this direction
                    moves.add(new ChessMove(position, newPosition, null));
                    break;
                } else {
                    // Own piece, stop in this direction
                    break;
                }
            }
        }

        return moves;
    }

    private boolean isValidPosition(int row, int column) {
        return row >= 1 && row <= 8 && column >= 1 && column <= 8;
    }
}