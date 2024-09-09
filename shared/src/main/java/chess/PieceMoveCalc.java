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
            case KING -> calculateKingMoves(piece, board, position);
            case QUEEN -> calculateSlidingPieceMoves(piece, board, position, QUEEN_DIRECTIONS);
            case BISHOP -> calculateSlidingPieceMoves(piece, board, position, BISHOP_DIRECTIONS);
            case KNIGHT -> calculateKnightMoves(piece, board, position);
            case ROOK -> calculateSlidingPieceMoves(piece, board, position, ROOK_DIRECTIONS);
            case PAWN -> calculatePawnMoves(piece, board, position);
        };
    }

    // Calculate moves for the King
    private Collection<ChessMove> calculateKingMoves(ChessPiece piece, ChessBoard board, ChessPosition position) {
        return calculateFixedPieceMoves(piece, board, position, KING_MOVES);
    }

    // Calculate moves for the Knight
    private Collection<ChessMove> calculateKnightMoves(ChessPiece piece, ChessBoard board, ChessPosition position) {
        return calculateFixedPieceMoves(piece, board, position, KNIGHT_MOVES);
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

        // Check each direction
        for (int[] direction : directions) {
            int row = currentRow;
            int column = currentColumn;

            // Continue moving in the current direction until blocked or off the board
            while (true) {
                row += direction[0];
                column += direction[1];

                if (!isValidPosition(row, column)) {
                    break;
                }

                ChessPosition newPosition = new ChessPosition(row, column);
                ChessPiece targetPiece = board.getPiece(newPosition);

                if (targetPiece == null) {
                    // Empty square, add move and continue
                    moves.add(new ChessMove(position, newPosition, null));
                } else {
                    // Occupied square
                    if (targetPiece.getTeamColor() != piece.getTeamColor()) {
                        // Can capture opponent's piece
                        moves.add(new ChessMove(position, newPosition, null));
                    }
                    // Stop checking this direction
                    break;
                }
            }
        }

        return moves;
    }

    // Calculate moves for Pawns
    private Collection<ChessMove> calculatePawnMoves(ChessPiece piece, ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new ArrayList<>();
        int currentRow = position.getRow();
        int currentColumn = position.getColumn();
        int direction = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;

        // Normal move (one square forward)
        addPawnMove(piece, board, moves, currentRow, currentColumn, currentRow + direction, currentColumn);

        // Initial double move
        if ((piece.getTeamColor() == ChessGame.TeamColor.WHITE && currentRow == 2) ||
                (piece.getTeamColor() == ChessGame.TeamColor.BLACK && currentRow == 7)) {
            addPawnMove(piece, board, moves, currentRow, currentColumn, currentRow + 2 * direction, currentColumn);
        }

        // Capture moves (including en passant)
        addPawnCapture(piece, board, moves, currentRow, currentColumn, currentRow + direction, currentColumn - 1);
        addPawnCapture(piece, board, moves, currentRow, currentColumn, currentRow + direction, currentColumn + 1);

        return moves;
    }

    // Helper method to add a normal pawn move
    private void addPawnMove(ChessPiece piece, ChessBoard board, Collection<ChessMove> moves,
                             int currentRow, int currentColumn, int newRow, int newColumn) {
        if (!isValidPosition(newRow, newColumn)) {
            return;
        }

        ChessPosition newPosition = new ChessPosition(newRow, newColumn);
        if (board.getPiece(newPosition) == null) {
            addPawnMoveWithPromotion(piece, moves, new ChessPosition(currentRow, currentColumn), newPosition);
        }
    }

    // Helper method to add a pawn capture move
    private void addPawnCapture(ChessPiece piece, ChessBoard board, Collection<ChessMove> moves,
                                int currentRow, int currentColumn, int newRow, int newColumn) {
        if (!isValidPosition(newRow, newColumn)) {
            return;
        }

        ChessPosition newPosition = new ChessPosition(newRow, newColumn);
        ChessPiece targetPiece = board.getPiece(newPosition);

        if (targetPiece != null && targetPiece.getTeamColor() != piece.getTeamColor()) {
            addPawnMoveWithPromotion(piece, moves, new ChessPosition(currentRow, currentColumn), newPosition);
        }

        // En passant capture (to be implemented)
        // if (newPosition equals the en passant target square) {
        //     moves.add(new ChessMove(new ChessPosition(currentRow, currentColumn), newPosition, null));
        // }
    }

    // Helper method to add pawn moves with promotion if applicable
    private void addPawnMoveWithPromotion(ChessPiece piece, Collection<ChessMove> moves, ChessPosition currentPosition, ChessPosition newPosition) {
        if ((piece.getTeamColor() == ChessGame.TeamColor.WHITE && newPosition.getRow() == 8) ||
                (piece.getTeamColor() == ChessGame.TeamColor.BLACK && newPosition.getRow() == 1)) {
            // Pawn promotion
            moves.add(new ChessMove(currentPosition, newPosition, ChessPiece.PieceType.QUEEN));
            moves.add(new ChessMove(currentPosition, newPosition, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(currentPosition, newPosition, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(currentPosition, newPosition, ChessPiece.PieceType.KNIGHT));
        } else {
            // Normal move
            moves.add(new ChessMove(currentPosition, newPosition, null));
        }
    }

    // Helper method to check if a position is within the chess board
    private boolean isValidPosition(int row, int column) {
        return row >= 1 && row <= 8 && column >= 1 && column <= 8;
    }
}