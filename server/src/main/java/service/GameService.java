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
}