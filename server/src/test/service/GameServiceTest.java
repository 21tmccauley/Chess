package service;

import chess.ChessGame;
import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameServiceTest {

    private GameService gameService;
    private DataAccess mockDataAccess;
    private AuthService mockAuthService;

    @BeforeEach
    void setUp() {
        mockDataAccess = mock(DataAccess.class);
        mockAuthService = mock(AuthService.class);
        gameService = new GameService(mockDataAccess, mockAuthService);
    }

    @Test
    void createGame() throws DataAccessException {
        String authToken = "validToken";
        String gameName = "testGame";
        AuthData authData = new AuthData(authToken, "testUser");
        GameData newGame = new GameData(1, null, null, gameName, new ChessGame());

        when(mockAuthService.getAuth(authToken)).thenReturn(authData);
        when(mockDataAccess.createGame(any(GameData.class))).thenReturn(1);

        int gameId = gameService.createGame(authToken, gameName);

        assertEquals(1, gameId);
        verify(mockDataAccess).createGame(any(GameData.class));
    }

    @Test
    void createGameInvalidAuth() throws DataAccessException {
        String authToken = "invalidToken";
        String gameName = "testGame";

        when(mockAuthService.getAuth(authToken)).thenReturn(null);

        assertThrows(DataAccessException.class, () -> gameService.createGame(authToken, gameName));
    }

    @Test
    void listGames() throws DataAccessException {
        String authToken = "validToken";
        AuthData authData = new AuthData(authToken, "testUser");
        Collection<GameData> expectedGames = Arrays.asList(
                new GameData(1, "white1", "black1", "game1", new ChessGame()),
                new GameData(2, "white2", "black2", "game2", new ChessGame())
        );

        when(mockAuthService.getAuth(authToken)).thenReturn(authData);
        when(mockDataAccess.listGames()).thenReturn(expectedGames);

        Collection<GameData> result = gameService.listGames(authToken);

        assertEquals(expectedGames, result);
    }

    @Test
    void listGamesInvalidAuth() throws DataAccessException {
        String authToken = "invalidToken";

        when(mockAuthService.getAuth(authToken)).thenReturn(null);

        assertThrows(DataAccessException.class, () -> gameService.listGames(authToken));
    }

    @Test
    void joinGame() throws DataAccessException {
        String authToken = "validToken";
        int gameId = 1;
        String playerColor = "WHITE";
        AuthData authData = new AuthData(authToken, "testUser");
        GameData game = new GameData(gameId, null, "blackUser", "testGame", new ChessGame());

        when(mockAuthService.getAuth(authToken)).thenReturn(authData);
        when(mockDataAccess.getGame(gameId)).thenReturn(game);

        gameService.joinGame(authToken, gameId, playerColor);

        verify(mockDataAccess).updateGame(any(GameData.class));
    }

    @Test
    void joinGameInvalidAuth() throws DataAccessException {
        String authToken = "invalidToken";
        int gameId = 1;
        String playerColor = "WHITE";

        when(mockAuthService.getAuth(authToken)).thenReturn(null);

        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken, gameId, playerColor));
    }

    @Test
    void joinGameNonexistentGame() throws DataAccessException {
        String authToken = "validToken";
        int gameId = 1;
        String playerColor = "WHITE";
        AuthData authData = new AuthData(authToken, "testUser");

        when(mockAuthService.getAuth(authToken)).thenReturn(authData);
        when(mockDataAccess.getGame(gameId)).thenReturn(null);

        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken, gameId, playerColor));
    }

    @Test
    void joinGameColorTaken() throws DataAccessException {
        String authToken = "validToken";
        int gameId = 1;
        String playerColor = "WHITE";
        AuthData authData = new AuthData(authToken, "testUser");
        GameData game = new GameData(gameId, "existingWhiteUser", null, "testGame", new ChessGame());

        when(mockAuthService.getAuth(authToken)).thenReturn(authData);
        when(mockDataAccess.getGame(gameId)).thenReturn(game);

        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken, gameId, playerColor));
    }

    @Test
    void joinGameInvalidColor() throws DataAccessException {
        String authToken = "validToken";
        int gameId = 1;
        String playerColor = "INVALID";
        AuthData authData = new AuthData(authToken, "testUser");
        GameData game = new GameData(gameId, null, null, "testGame", new ChessGame());

        when(mockAuthService.getAuth(authToken)).thenReturn(authData);
        when(mockDataAccess.getGame(gameId)).thenReturn(game);

        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken, gameId, playerColor));
    }
}