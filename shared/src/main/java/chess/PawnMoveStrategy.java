package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMoveStrategy implements MoveStrategy {

    @Override
    public Collection<ChessMove> calculateMoves(ChessPiece piece, ChessBoard board, ChessPosition position, GameState gameState) {
        Collection<ChessMove> moves = new ArrayList<>();
        int direction = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int currentRow = position.getRow();
        int currentColumn = position.getColumn();

        // Forward move
        addPawnMove(moves, board, position, currentRow + direction, currentColumn);

        // Initial two-square move
        if ((piece.getTeamColor() == ChessGame.TeamColor.WHITE && currentRow == 2) ||
                (piece.getTeamColor() == ChessGame.TeamColor.BLACK && currentRow == 7)) {
            // Check if the square immediately in front is empty
            ChessPosition nextPosition = new ChessPosition(currentRow + direction, currentColumn);
            if (board.getPiece(nextPosition) == null) {
                // If the immediate square is empty, check the double move square
                ChessPosition doublePosition = new ChessPosition(currentRow + 2 * direction, currentColumn);
                if (board.getPiece(doublePosition) == null) {
                    // Both squares are empty, so add the double move
                    addPawnMove(moves, board, position, currentRow + 2 * direction, currentColumn);
                }
            }
        }

        // Diagonal captures
        addPawnCapture(moves, board, position, currentRow + direction, currentColumn - 1);
        addPawnCapture(moves, board, position, currentRow + direction, currentColumn + 1);

        // En passant
        ChessPosition enPassantTarget = gameState.getEnPassantTarget();
        if (enPassantTarget != null) {
            if (Math.abs(enPassantTarget.getColumn() - currentColumn) == 1 &&
                    enPassantTarget.getRow() == currentRow + direction) {
                moves.add(new ChessMove(position, enPassantTarget, null));
            }
        }

        return moves;
    }

    private void addPawnMove(Collection<ChessMove> moves, ChessBoard board, ChessPosition from, int toRow, int toColumn) {
        if (!isValidPosition(toRow, toColumn)) return;

        ChessPosition to = new ChessPosition(toRow, toColumn);
        if (board.getPiece(to) == null) {
            if (toRow == 1 || toRow == 8) {
                // Promotion
                addPromotionMoves(moves, from, to);
            } else {
                moves.add(new ChessMove(from, to, null));
            }
        }
    }

    private void addPawnCapture(Collection<ChessMove> moves, ChessBoard board, ChessPosition from, int toRow, int toColumn) {
        if (!isValidPosition(toRow, toColumn)) return;

        ChessPosition to = new ChessPosition(toRow, toColumn);
        ChessPiece targetPiece = board.getPiece(to);
        if (targetPiece != null && targetPiece.getTeamColor() != board.getPiece(from).getTeamColor()) {
            if (toRow == 1 || toRow == 8) {
                // Promotion
                addPromotionMoves(moves, from, to);
            } else {
                moves.add(new ChessMove(from, to, null));
            }
        }
    }

    private void addPromotionMoves(Collection<ChessMove> moves, ChessPosition from, ChessPosition to) {
        moves.add(new ChessMove(from, to, ChessPiece.PieceType.QUEEN));
        moves.add(new ChessMove(from, to, ChessPiece.PieceType.ROOK));
        moves.add(new ChessMove(from, to, ChessPiece.PieceType.BISHOP));
        moves.add(new ChessMove(from, to, ChessPiece.PieceType.KNIGHT));
    }

    private boolean isValidPosition(int row, int column) {
        return row >= 1 && row <= 8 && column >= 1 && column <= 8;
    }
}