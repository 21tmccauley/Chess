package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class MySQLDataAccessTest {
    private MySQLDataAccess dataAccess;

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess = new MySQLDataAccess();
        // Clear all data before each test
        dataAccess.clearAll();
    }

    // User Tests
    @Test
    void createUserSuccess() throws DataAccessException {
        // Arrange
        UserData user = new UserData("testUser", "password123", "test@example.com");

        // Act & Assert
        assertDoesNotThrow(() -> dataAccess.createUser(user));
    }

    @Test
    void createUserDuplicateFails() throws DataAccessException {
        // Arrange
        UserData user = new UserData("testUser", "password123", "test@example.com");

        // Act & Assert
        dataAccess.createUser(user);
        assertThrows(DataAccessException.class, () -> dataAccess.createUser(user));
    }

    @Test
    void getUserExists() throws DataAccessException {
        // Arrange
        String plainTextPassword = "password123";
        UserData user = new UserData("testUser", plainTextPassword, "test@example.com");
        dataAccess.createUser(user);

        // Act
        UserData retrievedUser = dataAccess.getUser("testUser");

        // Assert
        assertNotNull(retrievedUser);
        assertEquals(user.username(), retrievedUser.username());
        assertEquals(user.email(), retrievedUser.email());

        // Verify the stored hash can be used to verify the original password
        boolean passwordVerified = BCrypt.checkpw(plainTextPassword, retrievedUser.password());
        assertTrue(passwordVerified, "Password hash verification failed");
    }

    @Test
    void getUserDoesNotExist() throws DataAccessException {
        // Act & Assert
        assertNull(dataAccess.getUser("nonexistentUser"));
    }

    // Auth Tests
    @Test
    void createAuthSuccess() throws DataAccessException {
        // Arrange
        UserData user = new UserData("testUser", "password123", "test@example.com");
        dataAccess.createUser(user);
        AuthData auth = new AuthData("testToken", "testUser");

        // Act & Assert
        assertDoesNotThrow(() -> dataAccess.createAuth(auth));
    }

    @Test
    void createAuthDuplicateTokenFails() throws DataAccessException {
        // Arrange
        UserData user = new UserData("testUser", "password123", "test@example.com");
        dataAccess.createUser(user);
        AuthData auth = new AuthData("testToken", "testUser");

        // Act & Assert
        dataAccess.createAuth(auth);
        assertThrows(DataAccessException.class, () -> dataAccess.createAuth(auth));
    }

    @Test
    void getAuthExists() throws DataAccessException {
        // Arrange
        UserData user = new UserData("testUser", "password123", "test@example.com");
        dataAccess.createUser(user);
        AuthData auth = new AuthData("testToken", "testUser");
        dataAccess.createAuth(auth);

        // Act
        AuthData retrievedAuth = dataAccess.getAuth("testToken");

        // Assert
        assertNotNull(retrievedAuth);
        assertEquals(auth.authToken(), retrievedAuth.authToken());
        assertEquals(auth.username(), retrievedAuth.username());
    }

    @Test
    void getAuthDoesNotExist() {
        // Act & Assert
        assertThrows(DataAccessException.class, () -> dataAccess.getAuth("nonexistentToken"));
    }

    @Test
    void deleteAuthSuccess() throws DataAccessException {
        // Arrange
        UserData user = new UserData("testUser", "password123", "test@example.com");
        dataAccess.createUser(user);
        AuthData auth = new AuthData("testToken", "testUser");
        dataAccess.createAuth(auth);

        // Act & Assert
        assertDoesNotThrow(() -> dataAccess.deleteAuth("testToken"));
        assertThrows(DataAccessException.class, () -> dataAccess.getAuth("testToken"));
    }

    @Test
    void deleteAuthNonexistent() {
        // Act & Assert
        assertThrows(DataAccessException.class, () -> dataAccess.deleteAuth("nonexistentToken"));
    }

    // Game Tests
    @Test
    void createGameSuccess() throws DataAccessException {
        // Arrange
        GameData game = new GameData(0, null, null, "testGame", new ChessGame());

        // Act
        int gameId = dataAccess.createGame(game);

        // Assert
        assertTrue(gameId > 0);
        GameData retrievedGame = dataAccess.getGame(gameId);
        assertNotNull(retrievedGame);
        assertEquals(game.gameName(), retrievedGame.gameName());
    }

    @Test
    void getGameExists() throws DataAccessException {
        // Arrange
        GameData game = new GameData(0, null, null, "testGame", new ChessGame());
        int gameId = dataAccess.createGame(game);

        // Act
        GameData retrievedGame = dataAccess.getGame(gameId);

        // Assert
        assertNotNull(retrievedGame);
        assertEquals(gameId, retrievedGame.gameID());
        assertEquals(game.gameName(), retrievedGame.gameName());
    }

    @Test
    void getGameDoesNotExist() {
        // Act & Assert
        assertThrows(DataAccessException.class, () -> dataAccess.getGame(999999));
    }

    @Test
    void updateGameSuccess() throws DataAccessException {
        // Arrange
        // First create the users that will be referenced in the game
        UserData whitePlayer = new UserData("whitePlayer", "password123", "white@example.com");
        UserData blackPlayer = new UserData("blackPlayer", "password123", "black@example.com");
        dataAccess.createUser(whitePlayer);
        dataAccess.createUser(blackPlayer);

        // Create initial game
        GameData game = new GameData(0, null, null, "testGame", new ChessGame());
        int gameId = dataAccess.createGame(game);

        // Create updated version with the existing users
        GameData updatedGame = new GameData(gameId, "whitePlayer", "blackPlayer", "testGame", new ChessGame());

        // Act & Assert
        assertDoesNotThrow(() -> dataAccess.updateGame(updatedGame));

        // Verify update
        GameData retrievedGame = dataAccess.getGame(gameId);
        assertEquals("whitePlayer", retrievedGame.whiteUsername());
        assertEquals("blackPlayer", retrievedGame.blackUsername());
    }

    @Test
    void updateGameDoesNotExist() throws DataAccessException {
        // Arrange
        // Create the users first
        UserData whitePlayer = new UserData("whitePlayer", "password123", "white@example.com");
        UserData blackPlayer = new UserData("blackPlayer", "password123", "black@example.com");
        dataAccess.createUser(whitePlayer);
        dataAccess.createUser(blackPlayer);

        // Try to update a non-existent game
        GameData nonexistentGame = new GameData(999999, "whitePlayer", "blackPlayer", "testGame", new ChessGame());

        // Act & Assert
        assertThrows(DataAccessException.class, () -> dataAccess.updateGame(nonexistentGame));
    }

    @Test
    void listGamesEmpty() throws DataAccessException {
        // Act
        Collection<GameData> games = dataAccess.listGames();

        // Assert
        assertTrue(games.isEmpty());
    }

    @Test
    void listGamesMultipleGames() throws DataAccessException {
        // Arrange
        dataAccess.createGame(new GameData(0, null, null, "game1", new ChessGame()));
        dataAccess.createGame(new GameData(0, null, null, "game2", new ChessGame()));
        dataAccess.createGame(new GameData(0, null, null, "game3", new ChessGame()));

        // Act
        Collection<GameData> games = dataAccess.listGames();

        // Assert
        assertEquals(3, games.size());
    }

    @Test
    void clearAllSuccess() throws DataAccessException {
        // Arrange
        UserData user = new UserData("testUser", "password123", "test@example.com");
        dataAccess.createUser(user);

        GameData game = new GameData(0, null, null, "testGame", new ChessGame());
        dataAccess.createGame(game);

        AuthData auth = new AuthData("testToken", "testUser");
        dataAccess.createAuth(auth);

        // Act
        dataAccess.clearAll();

        // Assert
        assertNull(dataAccess.getUser("testUser"));
        assertTrue(dataAccess.listGames().isEmpty());
        assertThrows(DataAccessException.class, () -> dataAccess.getAuth("testToken"));
    }

}