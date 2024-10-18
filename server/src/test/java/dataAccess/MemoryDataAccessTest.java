package dataAccess;

import chess.ChessGame;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    void getNonExistentUser() {
        assertThrows(DataAccessException.class, () -> dataAccess.getUser("nonExistentUser"));
    }

    @Test
    void createAndGetGame() throws DataAccessException {
        GameData game = new GameData(0, null, null, "TestGame", new ChessGame());
        int gameId = dataAccess.createGame(game);

        GameData retrievedGame = dataAccess.getGame(gameId);
        assertEquals(game.gameName(), retrievedGame.gameName());
        assertEquals(gameId, retrievedGame.gameID());
    }
}