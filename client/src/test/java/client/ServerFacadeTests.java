package client;

import org.junit.jupiter.api.*;
import server.Server;

public class ServerFacadeTests {
    private static Server server;
    private static ServerFacade facade;
    private String existingAuthToken;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void setUp() throws Exception {
        facade.clear();
        // Register and login a user for testing
        var authData = facade.register("testUser", "password", "test@email.com");
        existingAuthToken = authData.authToken();
    }

    @Test
    void clearSuccess() throws Exception {
        // Test that clear works
        Assertions.assertDoesNotThrow(() -> facade.clear());
    }

    @Test
    void clearTwiceSuccess() throws Exception {
        // Test that clearing twice works
        facade.clear();
        Assertions.assertDoesNotThrow(() -> facade.clear());
    }

    @Test
    void registerSuccess() throws Exception {
        var authData = facade.register("Player1", "password", "p1@email.com");
        Assertions.assertNotNull(authData);
        Assertions.assertNotNull(authData.authToken());
        Assertions.assertEquals("Player1", authData.username());
    }

    @Test
    void registerFailure() throws Exception {
        // Negative test case - duplicate registration
        facade.register("player1", "password", "p1@email.com");

        Assertions.assertThrows(Exception.class, () -> {
            facade.register("player1", "password", "p1@email.com");
        });
    }

    @Test
    void loginSuccess() throws Exception {
        // Register a user first
        facade.register("player1", "password", "p1@email.com");

        // Test successful login
        var authData = facade.login("player1", "password");
        Assertions.assertNotNull(authData);
        Assertions.assertNotNull(authData.authToken());
        Assertions.assertEquals("player1", authData.username());
    }

    @Test
    void loginFailure() throws Exception {
        // Test login with non-existent user
        Assertions.assertThrows(Exception.class, () -> {
            facade.login("nonexistentUser", "password");
        });
    }

    @Test
    void logoutSuccess() throws Exception {
        // Register and login to get auth token
        var authData = facade.register("player1", "password", "p1@email.com");

        // Test successful logout
        Assertions.assertDoesNotThrow(() -> {
            facade.logout(authData.authToken());
        });
    }

    @Test
    void logoutFailure() throws Exception {
        // Test logout with invalid auth token
        Assertions.assertThrows(Exception.class, () -> {
            facade.logout("invalidAuthToken");
        });
    }

    @Test
    void logoutTwiceFailure() throws Exception {
        // Register and login to get auth token
        var authData = facade.register("player1", "password", "p1@email.com");

        // Logout once successfully
        facade.logout(authData.authToken());

        // Try to logout again with same token
        Assertions.assertThrows(Exception.class, () -> {
            facade.logout(authData.authToken());
        });
    }

    @Test
    void createGameSuccess() throws Exception {
        int gameId = facade.createGame("testGame", existingAuthToken);
        Assertions.assertTrue(gameId > 0);
    }

    @Test
    void createGameFailureUnauthorized() throws Exception {
        Assertions.assertThrows(Exception.class, () -> {
            facade.createGame("testGame", "invalidAuthToken");
        });
    }

    @Test
    void listGamesEmpty() throws Exception {
        var games = facade.listGames(existingAuthToken);
        Assertions.assertTrue(games.isEmpty());
    }

    @Test
    void listGamesSuccess() throws Exception {
        // Create a game first
        facade.createGame("testGame1", existingAuthToken);
        facade.createGame("testGame2", existingAuthToken);

        var games = facade.listGames(existingAuthToken);
        Assertions.assertEquals(2, games.size());
    }

    @Test
    void listGamesFailureUnauthorized() throws Exception {
        Assertions.assertThrows(Exception.class, () -> {
            facade.listGames("invalidAuthToken");
        });
    }

    @Test
    void joinGameSuccessWhite() throws Exception {
        int gameId = facade.createGame("testGame", existingAuthToken);
        Assertions.assertDoesNotThrow(() -> {
            facade.joinGame(existingAuthToken, gameId, "WHITE");
        });
    }

    @Test
    void joinGameSuccessBlack() throws Exception {
        int gameId = facade.createGame("testGame", existingAuthToken);
        Assertions.assertDoesNotThrow(() -> {
            facade.joinGame(existingAuthToken, gameId, "BLACK");
        });
    }

    @Test
    void joinGameFailureInvalidAuth() throws Exception {
        int gameId = facade.createGame("testGame", existingAuthToken);

        Assertions.assertThrows(Exception.class, () -> {
            facade.joinGame("invalidAuthToken", gameId, "WHITE");
        });
    }

    @Test
    void joinGameFailureInvalidGame() throws Exception {
        Assertions.assertThrows(Exception.class, () -> {
            facade.joinGame(existingAuthToken, 999999, "WHITE");
        });
    }

    @Test
    void joinGameFailureColorTaken() throws Exception {
        // Create a game
        int gameId = facade.createGame("testGame", existingAuthToken);

        // Join as WHITE with first user
        facade.joinGame(existingAuthToken, gameId, "WHITE");

        // Create a second user
        var authData2 = facade.register("testUser2", "password2", "test2@email.com");

        // Try to join as WHITE with second user - should fail
        Assertions.assertThrows(Exception.class, () -> {
            facade.joinGame(authData2.authToken(), gameId, "WHITE");
        });
    }

    @Test
    void joinGameFailureInvalidColor() throws Exception {
        int gameId = facade.createGame("testGame", existingAuthToken);

        Assertions.assertThrows(Exception.class, () -> {
            facade.joinGame(existingAuthToken, gameId, "INVALID_COLOR");
        });
    }

//    @Test
//    void joinGameSuccessObserver() throws Exception {
//        int gameId = facade.createGame("testGame", existingAuthToken);
//
//        // Join as observer by omitting playerColor
//        Assertions.assertDoesNotThrow(() -> {
//            facade.joinGame(existingAuthToken, gameId, null);
//        });
//    }

//    @Test
//    void complexGameJoinScenario() throws Exception {
//        // Create a game
//        int gameId = facade.createGame("testGame", existingAuthToken);
//
//        // First user joins as WHITE
//        facade.joinGame(existingAuthToken, gameId, "WHITE");
//
//        // Create and login second user
//        var authData2 = facade.register("testUser2", "password2", "test2@email.com");
//
//        // Second user joins as BLACK
//        facade.joinGame(authData2.authToken(), gameId, "BLACK");
//
//        // Create and login third user
//        var authData3 = facade.register("testUser3", "password3", "test3@email.com");
//
//        // Third user joins as observer
//        facade.joinGame(authData3.authToken(), gameId, null);
//
//        // Verify game state through listGames
//        var games = facade.listGames(existingAuthToken);
//        var game = games.stream()
//                .filter(g -> g.gameID() == gameId)
//                .findFirst()
//                .orElseThrow(() -> new Exception("Game not found"));
//
//        Assertions.assertEquals("testUser", game.whiteUsername());
//        Assertions.assertEquals("testUser2", game.blackUsername());
//
//    }
}