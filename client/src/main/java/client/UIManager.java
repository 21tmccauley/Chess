package client;

import chess.*;
import ui.EscapeSequences;

import java.util.Collection;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages all user interface operations for the chess client.
 * This includes drawing the chess board, handling user input,
 * and displaying messages to the user.
 */
public class UIManager {
    private final Scanner scanner;

    // Constants for board drawing
    private static final String LIGHT_SQUARE = EscapeSequences.SET_BG_LIGHT;
    private static final String DARK_SQUARE = EscapeSequences.SET_BG_DARK;
    private static final String WHITE_PIECE_COLOR = EscapeSequences.SET_TEXT_COLOR_RED;
    private static final String BLACK_PIECE_COLOR = EscapeSequences.SET_TEXT_COLOR_BLUE;

    public UIManager() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Gets user input with a prompt.
     * @param prompt The message to display to the user
     * @return The user's input
     */
    public String promptForInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /**
     * Displays a message to the user.
     * @param message The message to display
     */
    public void displayMessage(String message) {
        System.out.println(message);
    }

    /**
     * Displays an error message in red.
     * @param error The error message to display
     */
    public void displayError(String error) {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED +
                "Error: " + error +
                EscapeSequences.RESET_TEXT_COLOR);
    }

    /**
     * Displays a notification message with visual distinction.
     * @param notification The notification message to display
     */
    public void displayNotification(String notification) {
        System.out.println("\n➢ " + notification);
    }

    /**
     * Clears the screen.
     */
    public void clearScreen() {
        System.out.print(EscapeSequences.ERASE_SCREEN);
    }

    /**
     * Draws the chess board from both perspectives.
     * @param game The current chess game
     */
    public void drawChessBoard(ChessGame game) {
        if (game == null) {
            displayError("No game to display");
            return;
        }

        System.out.println("\nBlack's Perspective:");
        drawBoardPerspective(game.getBoard(), true);

        System.out.println("\nWhite's Perspective:");
        drawBoardPerspective(game.getBoard(), false);
    }

    /**
     * Draws the board with highlighted squares for legal moves.
     * @param board The chess board
     * @param selectedPosition The position of the selected piece
     * @param validMoves Collection of valid moves for the selected piece
     */
    public void drawHighlightedBoard(ChessBoard board,
                                     ChessPosition selectedPosition,
                                     Collection<ChessMove> validMoves) {
        // Convert valid moves to a set of positions for easier lookup
        Set<ChessPosition> highlightedPositions = validMoves.stream()
                .map(ChessMove::getEndPosition)
                .collect(Collectors.toSet());

        System.out.println("\nBlack's Perspective:");
        drawBoardPerspectiveWithHighlights(true, board, selectedPosition, highlightedPositions);

        System.out.println("\nWhite's Perspective:");
        drawBoardPerspectiveWithHighlights(false, board, selectedPosition, highlightedPositions);
    }

    private void drawBoardPerspective(ChessBoard board, boolean blackPerspective) {
        // Print column headers
        printColumnHeaders(blackPerspective);

        for (int row = 0; row < 8; row++) {
            int displayRow = blackPerspective ? row + 1 : 8 - row;
            System.out.print(displayRow + "   ");

            for (int col = 0; col < 8; col++) {
                int actualRow = blackPerspective ? row : 7 - row;
                int actualCol = blackPerspective ? 7 - col : col;

                ChessPosition position = new ChessPosition(actualRow + 1, actualCol + 1);
                drawSquare(board, position);
            }
            System.out.println(EscapeSequences.RESET_ALL);
        }
    }

    private void drawBoardPerspectiveWithHighlights(boolean blackPerspective,
                                                    ChessBoard board,
                                                    ChessPosition selectedPosition,
                                                    Set<ChessPosition> highlightedPositions) {
        printColumnHeaders(blackPerspective);

        for (int row = 0; row < 8; row++) {
            int displayRow = blackPerspective ? row + 1 : 8 - row;
            System.out.print(displayRow + "   ");

            for (int col = 0; col < 8; col++) {
                int actualRow = blackPerspective ? row : 7 - row;
                int actualCol = blackPerspective ? 7 - col : col;

                ChessPosition currentPos = new ChessPosition(actualRow + 1, actualCol + 1);
                drawHighlightedSquare(board, currentPos, selectedPosition, highlightedPositions);
            }
            System.out.println(EscapeSequences.RESET_ALL);
        }
    }

    private void printColumnHeaders(boolean blackPerspective) {
        System.out.print("    ");
        for (int col = 0; col < 8; col++) {
            int displayCol = blackPerspective ? 7 - col : col;
            char colLabel = (char)('a' + displayCol);
            System.out.print(colLabel + "  ");
        }
        System.out.println();
    }

    private void drawSquare(ChessBoard board, ChessPosition position) {
        boolean isLightSquare = (position.getRow() + position.getColumn()) % 2 == 0;
        String background = isLightSquare ? LIGHT_SQUARE : DARK_SQUARE;
        System.out.print(background);

        ChessPiece piece = board.getPiece(position);
        if (piece != null) {
            String pieceColor = piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                    WHITE_PIECE_COLOR : BLACK_PIECE_COLOR;
            System.out.print(pieceColor + getPieceSymbol(piece));
        } else {
            System.out.print(EscapeSequences.EMPTY);
        }
        System.out.print(EscapeSequences.RESET_BG_COLOR);
    }

    private void drawHighlightedSquare(ChessBoard board,
                                       ChessPosition currentPos,
                                       ChessPosition selectedPosition,
                                       Set<ChessPosition> highlightedPositions) {
        boolean isSelected = currentPos.equals(selectedPosition);
        boolean isHighlighted = highlightedPositions.contains(currentPos);
        boolean isLightSquare = (currentPos.getRow() + currentPos.getColumn()) % 2 == 0;

        // Choose background color based on square status
        String background;
        if (isSelected) {
            background = EscapeSequences.SET_BG_COLOR_YELLOW;
        } else if (isHighlighted) {
            background = EscapeSequences.SET_BG_COLOR_GREEN;
        } else {
            background = isLightSquare ? LIGHT_SQUARE : DARK_SQUARE;
        }

        System.out.print(background);

        ChessPiece piece = board.getPiece(currentPos);
        if (piece != null) {
            String pieceColor = piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                    WHITE_PIECE_COLOR : BLACK_PIECE_COLOR;
            System.out.print(pieceColor + getPieceSymbol(piece));
        } else {
            System.out.print(EscapeSequences.EMPTY);
        }
        System.out.print(EscapeSequences.RESET_BG_COLOR);
    }

    private String getPieceSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                    EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                    EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                    EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                    EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                    EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                    EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
        };
    }

}