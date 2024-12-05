package client;

import chess.ChessPosition;

/**
 * Utility class for converting between algebraic chess notation and position objects.
 */
public class ChessPositionUtils {
    /**
     * Converts algebraic chess notation to a ChessPosition object.
     * Accepts positions like "e4", "a1", "h8" etc.
     *
     * @param algebraicPos Position in algebraic notation (e.g., "e4")
     * @return ChessPosition object, or null if the notation is invalid
     */
    public static ChessPosition parsePosition(String algebraicPos) {
        if (!isValidAlgebraicNotation(algebraicPos)) {
            return null;
        }

        char fileChar = Character.toLowerCase(algebraicPos.charAt(0));
        char rankChar = algebraicPos.charAt(1);

        int column = fileChar - 'a' + 1;
        int row = Character.getNumericValue(rankChar);

        return new ChessPosition(row, column);
    }

    /**
     * Validates if a string represents a valid algebraic chess position.
     *
     * @param algebraicPos Position to validate
     * @return true if the position is valid algebraic notation
     */
    public static boolean isValidAlgebraicNotation(String algebraicPos) {
        if (algebraicPos == null || algebraicPos.length() != 2) {
            return false;
        }

        char fileChar = Character.toLowerCase(algebraicPos.charAt(0));
        char rankChar = algebraicPos.charAt(1);

        return fileChar >= 'a' && fileChar <= 'h' &&
                rankChar >= '1' && rankChar <= '8';
    }

    /**
     * Converts a ChessPosition to algebraic notation.
     *
     * @param position The chess position to convert
     * @return String representing the position in algebraic notation (e.g. "e4")
     */
    public static String toAlgebraicNotation(ChessPosition position) {
        if (position == null ||
                position.getRow() < 1 || position.getRow() > 8 ||
                position.getColumn() < 1 || position.getColumn() > 8) {
            return null;
        }

        char file = (char)('a' + position.getColumn() - 1);
        char rank = (char)('0' + position.getRow());

        return String.valueOf(file) + rank;
    }
}