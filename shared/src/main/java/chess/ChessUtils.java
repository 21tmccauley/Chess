package chess;

public class ChessUtils {
    public static boolean isValidPosition(int row, int column) {
        return row >= 1 && row <= 8 && column >= 1 && column <= 8;
    }

    // Other utility methods can be added here
}
