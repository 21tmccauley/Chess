package server;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import service.*;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest {
    private UserService userService;
    private GameService gameService;
    private AuthService authService;
    private Dataaccess dataAccess;

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess = new MemoryDataaccess();
        authService = new AuthService(dataAccess);
        userService = new UserService(dataAccess, authService);
        gameService = new GameService(dataAccess, authService);
        dataAccess.clearAll(); // Clear before each test
    }

    @Test
    @DisplayName("Register User - Positive")
    void registerPositive() throws DataAccessException {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        AuthData result = userService.register(user);

        assertNotNull(result);
        assertEquals("testUser", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    @DisplayName("Register User - Negative (Duplicate Username)")
    void registerNegative() throws DataAccessException {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        userService.register(user);

        assertThrows(DataAccessException.class, () -> userService.register(user));
    }

    @Test
    @DisplayName("Login User - Positive")
    void loginPositive() throws DataAccessException {
        // Register first
        UserData user = new UserData("testUser", "password123", "test@example.com");
        userService.register(user);

        // Then login
        AuthData result = userService.login(user);

        assertNotNull(result);
        assertEquals("testUser", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    @DisplayName("Login User - Negative (Wrong Password)")
    void loginNegative() throws DataAccessException {
        // Register with one password
        UserData user = new UserData("testUser", "password123", "test@example.com");
        userService.register(user);

        // Try to login with different password
        UserData wrongUser = new UserData("testUser", "wrongpassword", "test@example.com");
        assertThrows(DataAccessException.class, () -> userService.login(wrongUser));
    }

    @Test
    @DisplayName("Logout User - Positive")
    void logoutPositive() throws DataAccessException {
        // Register and get auth token
        UserData user = new UserData("testUser", "password123", "test@example.com");
        AuthData auth = userService.register(user);

        // Logout
        assertDoesNotThrow(() -> userService.logout(auth.authToken()));

        // Verify auth token is invalid
        assertThrows(DataAccessException.class, () -> authService.getAuth(auth.authToken()));
    }

    @Test
    @DisplayName("Logout User - Negative (Invalid Token)")
    void logoutNegative() {
        assertThrows(DataAccessException.class, () -> userService.logout("invalid_token"));
    }

    @Test
    @DisplayName("Create Game - Positive")
    void createGamePositive() throws DataAccessException {
        // Register and get auth token
        UserData user = new UserData("testUser", "password123", "test@example.com");
        AuthData auth = userService.register(user);

        // Create game
        int gameId = gameService.createGame(auth.authToken(), "TestGame");

        assertTrue(gameId > 0);
        assertNotNull(dataAccess.getGame(gameId));
    }

    @Test
    @DisplayName("Create Game - Negative (Invalid Auth)")
    void createGameNegative() {
        assertThrows(DataAccessException.class,
                () -> gameService.createGame("invalid_token", "TestGame"));
    }

    @Test
    @DisplayName("List Games - Positive")
    void listGamesPositive() throws DataAccessException {
        // Register and get auth token
        UserData user = new UserData("testUser", "password123", "test@example.com");
        AuthData auth = userService.register(user);

        // Create a couple games
        gameService.createGame(auth.authToken(), "Game1");
        gameService.createGame(auth.authToken(), "Game2");

        var games = gameService.listGames(auth.authToken());

        assertNotNull(games);
        assertEquals(2, games.size());
    }

    @Test
    @DisplayName("List Games - Negative (Invalid Auth)")
    void listGamesNegative() {
        assertThrows(DataAccessException.class,
                () -> gameService.listGames("invalid_token"));
    }

    @Test
    @DisplayName("Join Game - Positive")
    void joinGamePositive() throws DataAccessException {
        // Register and get auth token
        UserData user = new UserData("testUser", "password123", "test@example.com");
        AuthData auth = userService.register(user);

        // Create game
        int gameId = gameService.createGame(auth.authToken(), "TestGame");

        // Join game
        assertDoesNotThrow(() ->
                gameService.joinGame(auth.authToken(), gameId, "WHITE")
        );

        // Verify join
        GameData game = dataAccess.getGame(gameId);
        assertEquals("testUser", game.whiteUsername());
    }

    @Test
    @DisplayName("Join Game - Negative (Color Taken)")
    void joinGameNegative() throws DataAccessException {
        // Register two users
        UserData user1 = new UserData("user1", "password123", "test1@example.com");
        UserData user2 = new UserData("user2", "password123", "test2@example.com");
        AuthData auth1 = userService.register(user1);
        AuthData auth2 = userService.register(user2);

        // Create game and join as WHITE with first user
        int gameId = gameService.createGame(auth1.authToken(), "TestGame");
        gameService.joinGame(auth1.authToken(), gameId, "WHITE");

        // Try to join as WHITE with second user
        assertThrows(DataAccessException.class, () ->
                gameService.joinGame(auth2.authToken(), gameId, "WHITE")
        );
    }

    @Test
    @DisplayName("Clear Application - Positive")
    void clearPositive() throws DataAccessException {
        // Add some data
        UserData user = new UserData("testUser", "password123", "test@example.com");
        AuthData auth = userService.register(user);
        gameService.createGame(auth.authToken(), "TestGame");

        // Clear
        assertDoesNotThrow(() -> dataAccess.clearAll());

        // Verify everything is cleared
        assertThrows(DataAccessException.class, () -> authService.getAuth(auth.authToken()));
        // Directly check if games are cleared without using auth
        assertTrue(dataAccess.listGames().isEmpty());
    }
}