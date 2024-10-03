package chess;

import java.util.ArrayList;
import java.util.Collection;

import static chess.ChessUtils.isValidPosition;

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

}