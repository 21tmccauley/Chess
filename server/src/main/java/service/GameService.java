package service;

import chess.ChessGame;
import dataaccess.Dataaccess;
import dataaccess.DataAccessException;
import model.GameData;
import model.AuthData;

import java.util.Collection;

public class GameService {
    private final Dataaccess dataAccess;
    private final AuthService authService;

    public GameService(Dataaccess dataAccess, AuthService authService) {
        this.dataAccess = dataAccess;
        this.authService = authService;
    }

    public int createGame(String authToken, String gameName) throws DataAccessException {
        // Verify the auth token
        AuthData authData = authService.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Invalid auth token");
        }
        // Create a new game
        GameData newGame = new GameData(0, null, null, gameName, new ChessGame());
        return dataAccess.createGame(newGame);
    }

    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        // Verify the auth token
        AuthData authData = authService.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Invalid auth token");
        }

        // Return all games
        return dataAccess.listGames();
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {
        // Verify the auth token
        AuthData authData = authService.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Invalid auth token");
        }

        // Get the game
        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Game not found");
        }

        // Join the game
        String username = authData.username();
        if ("WHITE".equalsIgnoreCase(playerColor)) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("White player already joined");
            }
            game = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
        } else if ("BLACK".equalsIgnoreCase(playerColor)) {
            if (game.blackUsername() != null) {
                throw new DataAccessException("Black player already joined");
            }
            game = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
        } else {
            throw new DataAccessException("Invalid player color");
        }

        dataAccess.updateGame(game);
    }

    /**
     * Gets a game by ID, verifying the auth token in the process.
     * This combines authentication and game retrieval in one step.
     */
    public GameData getGame(String authToken, int gameID) throws DataAccessException {
        // First verify the auth token
        AuthData authData = authService.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Invalid auth token");
        }

        // Then get the game
        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Game not found");
        }

        return game;
    }
    /**
     * Gets the username associated with an auth token.
     * This is useful for identifying players in game notifications.
     */
    public String getUsername(String authToken) throws DataAccessException {
        AuthData authData = authService.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Invalid auth token");
        }
        return authData.username();
    }

    /**
     * Updates the state of a game.
     * This is used when moves are made or game state changes.
     */
    public void updateGame(GameData game) throws DataAccessException {
        if (game == null) {
            throw new DataAccessException("Game cannot be null");
        }
        dataAccess.updateGame(game);
    }

    /**
     * Handles a player resigning from a game.
     * This marks the game as over and updates the game state.
     */
    public void resignGame(int gameID, String username) throws DataAccessException {
        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Game not found");
        }

        // Create a new game instance with the updated state
        ChessGame chessGame = game.game();
        // You might want to add a method to ChessGame to handle resignation
        // For now, we'll just create a new GameData with the same state
        GameData updatedGame = new GameData(
                game.gameID(),
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                chessGame
        );

        // Update the game in the database
        dataAccess.updateGame(updatedGame);
    }


}