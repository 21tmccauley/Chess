package client;

import chess.ChessPosition;

/**
 * Processes and validates user commands for the chess client
 */
public class CommandProcessor {
    private final GameManager gameManager;
    private final UIManager uiManager;
    private final ConnectionManager connectionManager;

    public CommandProcessor(GameManager gameManager, UIManager uiManager, ConnectionManager connectionManager) {
        this.gameManager = gameManager;
        this.uiManager = uiManager;
        this.connectionManager = connectionManager;
    }

    public void processCommand(String command) {
        String[] parts = command.toLowerCase().trim().split("\\s+");
        if (parts.length == 0) return;

        try {
            switch (parts[0]) {
                case "help" -> processHelp();
                case "move" -> processMove(parts);
                case "resign" -> processResign();
                case "leave" -> processLeave();
                case "highlight" -> processHighlight(parts);
                case "redraw" -> processRedraw();
                default -> uiManager.displayError("Unknown command. Type 'help' for available commands.");
            }
        } catch (Exception e) {
            uiManager.displayError("Error processing command: " + e.getMessage());
        }
    }

    private void processHelp() {
        uiManager.displayMessage("""
            Available commands:
            - help                 - Show this help message
            - move <from> <to>     - Make a move (e.g. 'move e2 e4')
            - resign              - Resign from the current game
            - leave               - Leave the current game
            - highlight <pos>     - Show legal moves for a piece
            - redraw             - Redraw the chess board
            """);
    }

    private void processMove(String[] parts) {
        if (parts.length != 3) {
            uiManager.displayError("Invalid move format. Use: move <from> <to> (e.g. 'move e2 e4')");
            return;
        }

        if (!gameManager.makeMove(parts[1], parts[2])) {
            uiManager.displayError("Invalid move. Please try again.");
        }
    }

    private void processResign() {
        String confirmation = uiManager.promptForInput("Are you sure you want to resign? (yes/no): ");
        if (confirmation.equalsIgnoreCase("yes")) {
            if (!gameManager.resignGame()) {
                uiManager.displayError("Unable to resign. Are you in an active game?");
            }
        } else {
            uiManager.displayMessage("Resignation cancelled.");
        }
    }

    private void processLeave() {
        if (gameManager.getCurrentGameId() == null) {
            uiManager.displayError("Not currently in a game.");
            return;
        }
        connectionManager.closeConnection();
        uiManager.displayMessage("Left the game.");
        uiManager.clearScreen();
    }

    private void processHighlight(String[] parts) {
        if (parts.length != 2) {
            uiManager.displayError("Invalid highlight format. Use: highlight <position> (e.g. 'highlight e2')");
            return;
        }

        var moves = gameManager.getLegalMoves(parts[1]);
        if (moves == null || moves.isEmpty()) {
            uiManager.displayError("No legal moves for that position.");
            return;
        }

        uiManager.clearScreen();
        uiManager.drawHighlightedBoard(
                gameManager.getCurrentGame().getBoard(),
                parsePosition(parts[1]),
                moves
        );
    }

    private void processRedraw() {
        if (gameManager.getCurrentGame() == null) {
            uiManager.displayError("No active game to display.");
            return;
        }
        uiManager.clearScreen();
        uiManager.drawChessBoard(gameManager.getCurrentGame());
    }

    private ChessPosition parsePosition(String algebraicPos) {
        if (algebraicPos == null || algebraicPos.length() != 2) {
            return null;
        }

        char fileChar = Character.toLowerCase(algebraicPos.charAt(0));
        if (fileChar < 'a' || fileChar > 'h') return null;
        int column = fileChar - 'a' + 1;

        char rankChar = algebraicPos.charAt(1);
        if (rankChar < '1' || rankChar > '8') return null;
        int row = Character.getNumericValue(rankChar);

        return new ChessPosition(row, column);
    }
}