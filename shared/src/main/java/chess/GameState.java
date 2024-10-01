package chess;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    private List<ChessMove> moveHistory;
    private boolean whiteCanCastleKingside;
    private boolean whiteCanCastleQueenside;
    private boolean blackCanCastleKingside;
    private boolean blackCanCastleQueenside;
    private ChessPosition enPassantTarget;

    public GameState() {
        moveHistory = new ArrayList<>();
        whiteCanCastleKingside = true;
        whiteCanCastleQueenside = true;
        blackCanCastleKingside = true;
        blackCanCastleQueenside = true;
        enPassantTarget = null;
    }

    public void addMove(ChessMove move) {
        moveHistory.add(move);
        updateCastlingRights(move);
        updateEnPassantTarget(move);
    }

    private void updateCastlingRights(ChessMove move) {
        // Implement logic to update castling rights based on the move
        // For example, if a king or rook moves, remove castling rights for that side
    }

    private void updateEnPassantTarget(ChessMove move) {
        // Implement logic to set en passant target if a pawn moves two squares
        // Otherwise, set it to null
    }

    public boolean canCastle(ChessGame.TeamColor color, boolean kingSide) {
        if (color == ChessGame.TeamColor.WHITE) {
            return kingSide ? whiteCanCastleKingside : whiteCanCastleQueenside;
        } else {
            return kingSide ? blackCanCastleKingside : blackCanCastleQueenside;
        }
    }

    public ChessPosition getEnPassantTarget() {
        return enPassantTarget;
    }

    // Add other necessary methods
}