package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public class PieceMoveCalc {
    private final Map<ChessPiece.PieceType, MoveStrategy> strategies;

    // Move patterns
    private static final int[][] KING_MOVES = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
    private static final int[][] KNIGHT_MOVES = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
    private static final int[][] BISHOP_DIRECTIONS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
    private static final int[][] ROOK_DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int[][] QUEEN_DIRECTIONS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    public PieceMoveCalc() {
        strategies = new EnumMap<>(ChessPiece.PieceType.class);
        strategies.put(ChessPiece.PieceType.KING, new FixedMoveStrategy(KING_MOVES, true));
        strategies.put(ChessPiece.PieceType.KNIGHT, new FixedMoveStrategy(KNIGHT_MOVES, false));
        strategies.put(ChessPiece.PieceType.QUEEN, new SlidingMoveStrategy(QUEEN_DIRECTIONS));
        strategies.put(ChessPiece.PieceType.BISHOP, new SlidingMoveStrategy(BISHOP_DIRECTIONS));
        strategies.put(ChessPiece.PieceType.ROOK, new SlidingMoveStrategy(ROOK_DIRECTIONS));
        strategies.put(ChessPiece.PieceType.PAWN, new PawnMoveStrategy());
    }

    public Collection<ChessMove> calculateMoves(ChessPiece piece, ChessBoard board, ChessPosition position, GameState gameState) {
        MoveStrategy strategy = strategies.get(piece.getPieceType());
        return strategy.calculateMoves(piece, board, position, gameState);
    }
}
