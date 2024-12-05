package client;

import chess.*;
import model.GameData;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;
import websocket.messages.LoadGameMessage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages game state and operations for the chess client.
 * Handles move validation, game state updates, and coordinates with the connection manager.
 */
public class GameManager {
    // Store all active games for quick lookup
    private final Map<Integer, GameData> activeGames;

    // Track current game state
    private Integer currentGameId;
    private ChessGame currentGame;
    private String currentPlayerColor;
    private final ConnectionManager connectionManager;

    // Dependencies


    public GameManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.activeGames = new HashMap<>();
    }

    /**
     * Sets up the current game context when joining or observing a game.
     *
     * @param gameId The ID of the game to set as current
     * @param playerColor The color of the current player ("WHITE"/"BLACK"), or null for observers
     */
    public void setCurrentGame(int gameId, String playerColor) {
        // Validate game ID
        if (gameId <= 0) {
            throw new IllegalArgumentException("Invalid game ID: " + gameId);
        }

        // For players, validate color. For observers, playerColor should be null
        if (playerColor != null) {
            playerColor = playerColor.toUpperCase();
            if (!playerColor.equals("WHITE") && !playerColor.equals("BLACK")) {
                throw new IllegalArgumentException("Invalid player color: " + playerColor);
            }
        }

        // Initialize the game context
        this.currentGameId = gameId;
        this.currentPlayerColor = playerColor;
        this.currentGame = null;  // Will be populated by first LoadGameMessage

        // Log the game setup
        System.out.println("Game context initialized - ID: " + gameId +
                ", Role: " + (playerColor != null ? playerColor + " player" : "Observer"));
    }

    /**
     * Updates the game state from a server message.
     * This method is called whenever a LoadGameMessage is received from the server,
     * ensuring the client's game state stays in sync with the server.
     *
     * @param message The LoadGameMessage containing the new game state
     * @throws IllegalStateException if no game is currently active
     */
    public void updateGameState(LoadGameMessage message) {
        // Verify we have an active game
        if (currentGameId == null) {
            throw new IllegalStateException("Cannot update game state - no active game");
        }

        // Verify the message contains a game
        if (message == null || message.getGame() == null) {
            throw new IllegalArgumentException("Invalid game update message");
        }

        // Update the current game state
        this.currentGame = message.getGame();

        // Update player turn information for validation
        ChessGame.TeamColor currentTurn = currentGame.getTeamTurn();

        // Log the game state update
        System.out.println("Game state updated - Turn: " +
                (currentTurn != null ? currentTurn.toString() : "Game Over"));

        // Check for game completion
        if (isGameOver()) {
            System.out.println("Game has ended");
        }

        // Optional: Verify game state is valid
        if (currentGame.getBoard() == null) {
            throw new IllegalStateException("Invalid game state - board is null");
        }
    }


    /**
     * Processes a move command from the user
     * @param startPos Starting position in algebraic notation (e.g., "e2")
     * @param endPos Ending position in algebraic notation (e.g., "e4")
     * @return true if the move was valid and sent to the server
     */
    // Original makeMove method updated to use ConnectionManager's sendCommand
    public boolean makeMove(String startPos, String endPos) {
        if (currentGameId == null || currentGame == null) {
            throw new IllegalStateException("No active game");
        }

        // Convert algebraic notation to positions
        ChessPosition start = ChessPositionUtils.parsePosition(startPos);
        ChessPosition end = ChessPositionUtils.parsePosition(endPos);

        if (start == null || end == null) {
            return false;
        }

        // Verify it's the player's turn
        ChessGame.TeamColor currentTurn = currentGame.getTeamTurn();
        if (!isPlayersTurn(currentTurn)) {
            return false;
        }

        // Create and validate the move
        ChessMove move = new ChessMove(start, end, null);
        Collection<ChessMove> validMoves = currentGame.validMoves(start);
        if (validMoves == null || !validMoves.contains(move)) {
            return false;
        }

        try {
            // Create and send the move command through ConnectionManager
            MakeMoveCommand moveCommand = new MakeMoveCommand(
                    connectionManager.getAuthToken(),
                    currentGameId,
                    move
            );
            connectionManager.sendCommand(moveCommand);
            return true;
        } catch (Exception e) {
            System.out.println("Error sending move command: " + e.getMessage());
            return false;
        }
    }

    /**
     * Sends a resign command to the server
     * @return true if the command was sent successfully
     */
    public boolean resignGame() {
        if (currentGameId == null || currentPlayerColor == null) {
            return false;  // Can't resign if not in a game or just observing
        }

        try {
            connectionManager.sendCommand(new ResignCommand(connectionManager.getAuthToken(), currentGameId));
            return true;
        } catch (Exception e) {
            System.out.println("Error sending resign command: " + e.getMessage());
            return false;
        }
    }


    /**
     * Gets all legal moves for a piece at the specified position
     * @param positionString Position in algebraic notation (e.g., "e2")
     * @return Collection of legal moves, or null if no valid moves
     */
    public Collection<ChessMove> getLegalMoves(String positionString) {
        if (currentGame == null) {
            return null;
        }

        ChessPosition position = ChessPositionUtils.parsePosition(positionString);
        if (position == null) {
            return null;
        }

        return currentGame.validMoves(position);
    }

    /**
     * Checks if the current game is in a terminal state (checkmate/stalemate)
     * @return true if the game is over
     */
    public boolean isGameOver() {
        if (currentGame == null) {
            return false;
        }

        return currentGame.isInCheckmate(ChessGame.TeamColor.WHITE) ||
                currentGame.isInCheckmate(ChessGame.TeamColor.BLACK) ||
                currentGame.isInStalemate(ChessGame.TeamColor.WHITE) ||
                currentGame.isInStalemate(ChessGame.TeamColor.BLACK);
    }

    /**
     * Updates the collection of active games
     * @param games Collection of current games from server
     */

    /**
     * Gets the current game state
     * @return The current ChessGame, or null if no active game
     */
    public ChessGame getCurrentGame() {
        return currentGame;
    }

    /**
     * Gets the current game ID
     * @return The current game ID, or null if no active game
     */
    public Integer getCurrentGameId() {
        return currentGameId;
    }


    /**
     * Checks if it's the current player's turn
     * @param currentTurn The color whose turn it is
     * @return true if it's the player's turn
     */
    private boolean isPlayersTurn(ChessGame.TeamColor currentTurn) {
        if (currentPlayerColor == null) {
            return false;  // Observers can't make moves
        }

        return (currentTurn == ChessGame.TeamColor.WHITE && currentPlayerColor.equals("WHITE")) ||
                (currentTurn == ChessGame.TeamColor.BLACK && currentPlayerColor.equals("BLACK"));
    }

    /**
     * Clears the current game state, resetting all game-related fields.
     * This is used when leaving a game or when a connection attempt fails.
     */
    public void clearGameState() {
        this.currentGameId = null;
        this.currentPlayerColor = null;
        this.currentGame = null;
        System.out.println("Debug: Game state cleared");
    }
}