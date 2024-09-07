package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PieceMoveCalc {
    public Collection<ChessMove> calculateMoves(ChessPiece piece, ChessBoard board, ChessPosition position) {
        return switch (piece.getPieceType()) {
            case KING -> calculateKingMoves(piece, board, position);
            case QUEEN -> calculateSlidingPieceMoves(piece, board, position, QUEEN_DIRECTIONS);
            case BISHOP -> calculateSlidingPieceMoves(piece, board, position, BISHOP_DIRECTIONS);
            case KNIGHT -> calculateKnightMoves(piece, board, position);
            case ROOK -> calculateSlidingPieceMoves(piece, board, position, ROOK_DIRECTIONS);
            case PAWN -> calculatePawnMoves(piece, board, position);
        };
    }

    private static final int[][] BISHOP_DIRECTIONS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
    private static final int[][] ROOK_DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int[][] QUEEN_DIRECTIONS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private Collection<ChessMove> calculateSlidingPieceMoves(ChessPiece piece, ChessBoard board, ChessPosition position, int[][] directions) {
        Collection<ChessMove> moves = new ArrayList<>();
        for (int[] direction : directions) {
            int row = position.getRow();
            int column = position.getColumn();

            while (true) {
                row += direction[0];
                column += direction[1];

                // make sure the move is on the board
                if (row < 1 || row > 8 || column < 1 || column > 8) {
                    break;
                }

                ChessPosition newPosition = new ChessPosition(row, column);
                ChessPiece targetPiece = board.getPiece(newPosition);

                if (targetPiece == null) {
                    // this position is empty and can be added as a valid move
                    moves.add(new ChessMove(position, newPosition, null));
                } else if (targetPiece.getTeamColor() != piece.getTeamColor()) {
                    // the target square is occupied by an enemy piece that we can take
                    moves.add(new ChessMove(position, newPosition, null));
                    break;
                } else {
                    // we ran into our own piece, so we stop checking in that direction
                    break;
                }
            }
        }
        return moves;
    }

    private Collection<ChessMove> calculateKingMoves(ChessPiece piece, ChessBoard board, ChessPosition position) {
        // Implementation for King's moves
        return new ArrayList<>();
    }

    private Collection<ChessMove> calculateKnightMoves(ChessPiece piece, ChessBoard board, ChessPosition position) {
        // Implementation for Knight's moves
        return new ArrayList<>();
    }

    private Collection<ChessMove> calculatePawnMoves(ChessPiece piece, ChessBoard board, ChessPosition position) {
        // Implementation for Pawn's moves
        return new ArrayList<>();
    }
}