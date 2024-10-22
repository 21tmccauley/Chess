package dataAccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class MemoryDataAccessTest {
    private DataAccess dataAccess;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
    }

    @Test
    void createUserAndGetUser() throws DataAccessException {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        dataAccess.createUser(user);

        UserData retrievedUser = dataAccess.getUser("testUser");
        assertEquals(user, retrievedUser);
    }

    @Test
    void createDuplicateUser() {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        assertDoesNotThrow(() -> dataAccess.createUser(user));
        assertThrows(DataAccessException.class, () -> dataAccess.createUser(user));
    }

    @Test
    void getNonExistentUser() throws DataAccessException {  // Add throws declaration
        // Now handles the potential exception
        assertNull(dataAccess.getUser("nonExistentUser"));
    }

    @Test
    void createAndGetGame() throws DataAccessException {
        GameData game = new GameData(0, null, null, "TestGame", new ChessGame());
        int gameId = dataAccess.createGame(game);

        GameData retrievedGame = dataAccess.getGame(gameId);
        assertEquals(game.gameName(), retrievedGame.gameName());
        assertEquals(gameId, retrievedGame.gameID());
    }
    @Test
    void updateGame() throws DataAccessException {
        GameData game = new GameData(0, null, null, "TestGame", new ChessGame());
        int gameId = dataAccess.createGame(game);

        GameData updatedGame = new GameData(gameId, "white", "black", "UpdatedGame", new ChessGame());
        dataAccess.updateGame(updatedGame);

        GameData retrievedGame = dataAccess.getGame(gameId);
        assertEquals("UpdatedGame", retrievedGame.gameName());
        assertEquals("white", retrievedGame.whiteUsername());
        assertEquals("black", retrievedGame.blackUsername());
    }

    @Test
    void listGames() throws DataAccessException {
        dataAccess.createGame(new GameData(0, null, null, "Game1", new ChessGame()));
        dataAccess.createGame(new GameData(0, null, null, "Game2", new ChessGame()));

        Collection<GameData> games = dataAccess.listGames();
        assertEquals(2, games.size());
    }

    @Test
    void createAndGetAuth() throws DataAccessException {
        AuthData auth = new AuthData("token123", "testUser");
        dataAccess.createAuth(auth);

        AuthData retrievedAuth = dataAccess.getAuth("token123");
        assertEquals(auth, retrievedAuth);
    }

    @Test
    void deleteAuth() throws DataAccessException {
        AuthData auth = new AuthData("token123", "testUser");
        dataAccess.createAuth(auth);

        dataAccess.deleteAuth("token123");
        assertThrows(DataAccessException.class, () -> dataAccess.getAuth("token123"));
    }

    @Test
    void clearAll() throws DataAccessException {
        dataAccess.createUser(new UserData("testUser", "password", "email"));
        dataAccess.createGame(new GameData(0, null, null, "TestGame", new ChessGame()));
        dataAccess.createAuth(new AuthData("token123", "testUser"));

        dataAccess.clearAll();

        assertNull(dataAccess.getUser("testUser"));  // This line already has exception handling from method declaration
        assertThrows(DataAccessException.class, () -> dataAccess.getGame(1));
        assertThrows(DataAccessException.class, () -> dataAccess.getAuth("token123"));
        assertTrue(dataAccess.listGames().isEmpty());
    }
}