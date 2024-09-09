package chess;
import java.util.Objects;
/**
 * Represents moving a chess piece on a chessboard
 */
public class ChessMove {
    private final ChessPosition startPosition;
    private final ChessPosition endPosition;
    private final ChessPiece.PieceType promotionPiece;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition, ChessPiece.PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return endPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotionPiece;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ChessMove other) {
            return this.startPosition.equals(other.startPosition) &&
                    this.endPosition.equals(other.endPosition) &&
                    this.promotionPiece == other.promotionPiece;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPosition, endPosition, promotionPiece);
    }

    @Override
    public String toString() {
        char startFile = (char) ('a' + startPosition.getColumn() - 1);
        char endFile = (char) ('a' + endPosition.getColumn() - 1);

        String move = "" + startFile + startPosition.getRow() +
                "-" + endFile + endPosition.getRow();

        if (promotionPiece != null) {
            move += "=" + promotionPiece;
        }

        return move;
    }
}