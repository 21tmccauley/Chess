package service;

import dataaccess.MemoryDataaccess;
import dataaccess.Dataaccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {
    private GameService gameService;
    private Dataaccess dataAccess;
    private AuthService authService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        // Use real implementations with in-memory data store
        dataAccess = new MemoryDataaccess();
        authService = new AuthService(dataAccess);
        gameService = new GameService(dataAccess, authService);
        userService = new UserService(dataAccess, authService);

        // Clear data before each test
        try {
            dataAccess.clearAll();
        } catch (DataAccessException e) {
            fail("Failed to clear database before test");
        }
    }

    private AuthData createTestUser(String username) throws DataAccessException {
        UserData userData = new UserData(username, "password", "email@example.com");
        return userService.register(userData);
    }

    @Test
    void createGameSuccess() throws DataAccessException {
        // Arrange
        AuthData authData = createTestUser("testUser");
        String gameName = "testGame";

        // Act
        int gameId = gameService.createGame(authData.authToken(), gameName);

        // Assert
        assertTrue(gameId > 0);
        GameData game = dataAccess.getGame(gameId);
        assertNotNull(game);
        assertEquals(gameName, game.gameName());
        assertNull(game.whiteUsername());
        assertNull(game.blackUsername());
        assertNotNull(game.game());
    }

    @Test
    void createGameInvalidAuthFails() throws DataAccessException {
        // Arrange
        String invalidToken = "invalidToken";
        String gameName = "testGame";

        // Act & Assert
        assertThrows(DataAccessException.class, () -> gameService.createGame(invalidToken, gameName));
    }

    @Test
    void listGamesSuccess() throws DataAccessException {
        // Arrange
        AuthData authData = createTestUser("testUser");

        // Create some test games
        gameService.createGame(authData.authToken(), "game1");
        gameService.createGame(authData.authToken(), "game2");

        // Act
        Collection<GameData> games = gameService.listGames(authData.authToken());

        // Assert
        assertNotNull(games);
        assertEquals(2, games.size());
        games.forEach(game -> {
            assertNotNull(game.gameID());
            assertNotNull(game.gameName());
            assertNotNull(game.game());
        });
    }

    @Test
    void listGamesInvalidAuthFails() throws DataAccessException {
        // Arrange
        String invalidToken = "invalidToken";

        // Act & Assert
        assertThrows(DataAccessException.class, () -> gameService.listGames(invalidToken));
    }

    @Test
    void joinGameSuccess() throws DataAccessException {
        // Arrange
        AuthData authData = createTestUser("testUser");
        int gameId = gameService.createGame(authData.authToken(), "testGame");

        // Act
        gameService.joinGame(authData.authToken(), gameId, "WHITE");

        // Assert
        GameData game = dataAccess.getGame(gameId);
        assertNotNull(game);
        assertEquals("testUser", game.whiteUsername());
        assertNull(game.blackUsername());
    }

    @Test
    void joinGameInvalidAuthFails() throws DataAccessException {
        // Arrange
        AuthData authData = createTestUser("testUser");
        int gameId = gameService.createGame(authData.authToken(), "testGame");
        String invalidToken = "invalidToken";

        // Act & Assert
        assertThrows(DataAccessException.class,
                () -> gameService.joinGame(invalidToken, gameId, "WHITE"));
    }

    @Test
    void joinGameNonexistentGameFails() throws DataAccessException {
        // Arrange
        AuthData authData = createTestUser("testUser");
        int nonexistentGameId = 9999;

        // Act & Assert
        assertThrows(DataAccessException.class,
                () -> gameService.joinGame(authData.authToken(), nonexistentGameId, "WHITE"));
    }

    @Test
    void joinGameColorTakenFails() throws DataAccessException {
        // Arrange
        AuthData user1 = createTestUser("user1");
        AuthData user2 = createTestUser("user2");
        int gameId = gameService.createGame(user1.authToken(), "testGame");

        // User1 takes WHITE
        gameService.joinGame(user1.authToken(), gameId, "WHITE");

        // Act & Assert - User2 tries to take WHITE
        assertThrows(DataAccessException.class,
                () -> gameService.joinGame(user2.authToken(), gameId, "WHITE"));
    }

    @Test
    void joinGameInvalidColorFails() throws DataAccessException {
        // Arrange
        AuthData authData = createTestUser("testUser");
        int gameId = gameService.createGame(authData.authToken(), "testGame");

        // Act & Assert
        assertThrows(DataAccessException.class,
                () -> gameService.joinGame(authData.authToken(), gameId, "INVALID"));
    }

    @Test
    void joinGameDifferentColorsSuccess() throws DataAccessException {
        // Arrange
        AuthData user1 = createTestUser("user1");
        AuthData user2 = createTestUser("user2");
        int gameId = gameService.createGame(user1.authToken(), "testGame");

        // Act
        gameService.joinGame(user1.authToken(), gameId, "WHITE");
        gameService.joinGame(user2.authToken(), gameId, "BLACK");

        // Assert
        GameData game = dataAccess.getGame(gameId);
        assertNotNull(game);
        assertEquals("user1", game.whiteUsername());
        assertEquals("user2", game.blackUsername());
    }
}