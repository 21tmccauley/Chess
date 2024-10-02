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

    private void addCastlingMoves(ChessPiece piece, ChessBoard board, ChessPosition position, GameState gameState, Collection<ChessMove> moves) {
        ChessGame.TeamColor color = piece.getTeamColor();
        int row = color == ChessGame.TeamColor.WHITE ? 1 : 8;

        if (gameState.canCastle(color, true)) {
            // Check kingside castling
            if (isCastlingPathClear(board, row, 5, 7)) {
                moves.add(new ChessMove(position, new ChessPosition(row, 7), null));
            }
        }

        if (gameState.canCastle(color, false)) {
            // Check queenside castling
            if (isCastlingPathClear(board, row, 3, 4)) {
                moves.add(new ChessMove(position, new ChessPosition(row, 3), null));
            }
        }
    }

    private boolean isCastlingPathClear(ChessBoard board, int row, int startCol, int endCol) {
        for (int col = startCol; col <= endCol; col++) {
            if (board.getPiece(new ChessPosition(row, col)) != null) {
                return false;
            }
        }
        return true;
    }
}