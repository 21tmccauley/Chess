package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PieceMoveCalc {

    // Predefined move patterns for different piece types
    private static final int[][] KING_MOVES = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1},{0, 1}, {1, -1},  {1, 0},  {1, 1}};
    private static final int[][] KNIGHT_MOVES = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2},  {1, 2}, {2, -1},  {2, 1}};
    private static final int[][] BISHOP_DIRECTIONS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
    private static final int[][] ROOK_DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int[][] QUEEN_DIRECTIONS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    // Main method to calculate moves for any chess piece
    public Collection<ChessMove> calculateMoves(ChessPiece piece, ChessBoard board, ChessPosition position) {
        // Use a switch expression to determine which method to call based on the piece type
        return switch (piece.getPieceType()) {
            case KING -> calculateFixedPieceMoves(piece, board, position, KING_MOVES);
            case QUEEN -> calculateSlidingPieceMoves(piece, board, position, QUEEN_DIRECTIONS);
            case BISHOP -> calculateSlidingPieceMoves(piece, board, position, BISHOP_DIRECTIONS);
            case KNIGHT -> calculateFixedPieceMoves(piece, board, position, KNIGHT_MOVES);
            case ROOK -> calculateSlidingPieceMoves(piece, board, position, ROOK_DIRECTIONS);
            case PAWN -> calculatePawnMoves(piece, board, position);
        };
    }

    // Generic method to calculate moves for pieces with fixed movement patterns (King and Knight)
    private Collection<ChessMove> calculateFixedPieceMoves(ChessPiece piece, ChessBoard board, ChessPosition position, int[][] moveOffsets) {
        Collection<ChessMove> moves = new ArrayList<>();
        int currentRow = position.getRow();
        int currentColumn = position.getColumn();

        // Check each possible move offset
        for (int[] move : moveOffsets) {
            int newRow = currentRow + move[0];
            int newColumn = currentColumn + move[1];

            // Ensure the new position is on the board
            if (isValidPosition(newRow, newColumn)) {
                ChessPosition newPosition = new ChessPosition(newRow, newColumn);
                ChessPiece targetPiece = board.getPiece(newPosition);

                // Add move if the target square is empty or occupied by an opponent's piece
                if (targetPiece == null || targetPiece.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(position, newPosition, null));
                }
            }
        }

        return moves;
    }

    // Calculate moves for sliding pieces (Queen, Bishop, Rook)
    private Collection<ChessMove> calculateSlidingPieceMoves(ChessPiece piece, ChessBoard board, ChessPosition position, int[][] directions) {
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

                if (targetPiece == null || targetPiece.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(position, newPosition, null));
                    if (targetPiece != null) {
                        break;  // Stop after capturing an opponent's piece
                    }
                } else {
                    break;  // Stop if we encounter our own piece
                }
            }
        }

        return moves;
    }

    private Collection<ChessMove> calculatePawnMoves(ChessPiece piece, ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new ArrayList<>();

        // Set up variables
        int row = position.getRow();
        int column = position.getColumn();
        int direction = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;

        // Forward moves
        int newRow = row + direction;
        if (isValidPosition(newRow, column)) {
            // Single step
            ChessPosition newPosition = new ChessPosition(newRow, column);
            if (board.getPiece(newPosition) == null) {
                addMoveWithPromotion(moves, position, newPosition);

                // Double step
                if ((piece.getTeamColor() == ChessGame.TeamColor.WHITE && row == 2) ||
                        (piece.getTeamColor() == ChessGame.TeamColor.BLACK && row == 7)) {
                    int doubleRow = newRow + direction;
                    ChessPosition doublePosition = new ChessPosition(doubleRow, column);
                    if (board.getPiece(doublePosition) == null) {
                        moves.add(new ChessMove(position, doublePosition, null));
                    }
                }
            }
        }

        // Captures
        for (int colOffset : new int[]{-1, 1}) {
            int newCol = column + colOffset;
            if (isValidPosition(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(newPosition);
                if (targetPiece != null && targetPiece.getTeamColor() != piece.getTeamColor()) {
                    addMoveWithPromotion(moves, position, newPosition);
                }
            }
        }

        return moves;
    }

    private void addMoveWithPromotion(Collection<ChessMove> moves, ChessPosition from, ChessPosition to) {
        if ((to.getRow() == 8 && from.getRow() == 7) || (to.getRow() == 1 && from.getRow() == 2)) {
            // Promotion
            moves.add(new ChessMove(from, to, ChessPiece.PieceType.QUEEN));
            moves.add(new ChessMove(from, to, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(from, to, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(from, to, ChessPiece.PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(from, to, null));
        }
    }

    // Helper method to check if a position is within the chess board
    private boolean isValidPosition(int row, int column) {
        return row >= 1 && row <= 8 && column >= 1 && column <= 8;
    }
}