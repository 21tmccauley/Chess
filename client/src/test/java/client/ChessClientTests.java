//package client;
//
//import org.junit.jupiter.api.*;
//import static org.junit.jupiter.api.Assertions.*;
//import server.Server;
//import model.AuthData;
//
//public class ChessClientTests {
//    private static Server server;
//    private static int port;
//    private ChessClient client;
//    private static ServerFacade facade;
//
//    @BeforeAll
//    public static void init() {
//        server = new Server();
//        port = server.run(0);
//        System.out.println("Started test HTTP server on " + port);
//        facade = new ServerFacade(port);
//    }
//
//    @AfterAll
//    static void stopServer() {
//        server.stop();
//    }
//
//    @BeforeEach
//    void setup() throws Exception {
//        facade.clear();
//        client = new ChessClient(port);
//        System.out.println("IT made it");
//    }
//
//    // Pre-login State Tests
//    @Test
//    void initialState_shouldBePreLogin() {
//        assertEquals(ChessClient.State.PRELOGIN, client.getState());
//    }
//
//    @Test
//    void preLoginHelp_shouldShowCorrectCommands() {
//        String result = client.evaluateCommand("help");
//        assertTrue(result.contains("login"));
//        assertTrue(result.contains("register"));
//        assertTrue(result.contains("quit"));
//    }
//
//    // Login Tests
//    @Test
//    void login_withValidCredentials_shouldSucceed() throws Exception {
//        // Register a user first
//        facade.register("testuser", "testpass", "test@email.com");
//
//        // Test login
//        client.setTestInput("testuser", "testpass");
//        String result = client.evaluateCommand("login");
//
//        assertTrue(result.contains("Logged in successfully"));
//        assertEquals(ChessClient.State.POSTLOGIN, client.getState());
//        assertNotNull(client.getAuthToken());
//    }
//
//    @Test
//    void login_withInvalidCredentials_shouldFail() {
//        client.setTestInput("wronguser", "wrongpass");
//        String result = client.evaluateCommand("login");
//
//        assertTrue(result.contains("Error"));
//        assertEquals(ChessClient.State.PRELOGIN, client.getState());
//        assertNull(client.getAuthToken());
//    }
//
//    // Register Tests
//    @Test
//    void register_withValidInfo_shouldSucceed() {
//        client.setTestInput("newuser", "newpass", "new@email.com");
//        String result = client.evaluateCommand("register");
//
//        assertTrue(result.contains("Registration successful"));
//        assertEquals(ChessClient.State.POSTLOGIN, client.getState());
//        assertNotNull(client.getAuthToken());
//    }
//
//    @Test
//    void register_withDuplicateUsername_shouldFail() throws Exception {
//        // Register first user
//        facade.register("testuser", "testpass", "test@email.com");
//
//        // Try to register same username
//        client.setTestInput("testuser", "different", "other@email.com");
//        String result = client.evaluateCommand("register");
//
//        assertTrue(result.contains("Error"));
//        assertEquals(ChessClient.State.PRELOGIN, client.getState());
//    }
//
//    // Post-login State Tests
//    @Test
//    void postLoginHelp_shouldShowCorrectCommands() throws Exception {
//        // Login first
//        AuthData auth = facade.register("testuser", "testpass", "test@email.com");
//        client.setAuthTokenForTest(auth.authToken());
//
//        String result = client.evaluateCommand("help");
//        assertTrue(result.contains("logout"));
//        assertTrue(result.contains("create game"));
//        assertTrue(result.contains("list games"));
//        assertTrue(result.contains("join game"));
//        assertTrue(result.contains("observe"));
//    }
//
//    // Logout Tests
//    @Test
//    void logout_shouldReturnToPreLoginState() throws Exception {
//        // Login first
//        AuthData auth = facade.register("testuser", "testpass", "test@email.com");
//        client.setAuthTokenForTest(auth.authToken());
//
//        String result = client.evaluateCommand("logout");
//
//        assertTrue(result.contains("Logged out successfully"));
//        assertEquals(ChessClient.State.PRELOGIN, client.getState());
//        assertNull(client.getAuthToken());
//    }
//
//    // Game Creation Tests
//    @Test
//    void createGame_shouldSucceed() throws Exception {
//        // Login first
//        AuthData auth = facade.register("testuser", "testpass", "test@email.com");
//        client.setAuthTokenForTest(auth.authToken());
//
//        client.setTestInput("My Game");
//        String result = client.evaluateCommand("create game");
//
//        assertTrue(result.contains("Game created successfully"));
//    }
//
//    // List Games Tests
//    @Test
//    void listGames_shouldShowAllGames() throws Exception {
//        // Login and create a game
//        AuthData auth = facade.register("testuser", "testpass", "test@email.com");
//        client.setAuthTokenForTest(auth.authToken());
//        facade.createGame("Test Game", auth.authToken());
//
//        String result = client.evaluateCommand("list games");
//
//        assertTrue(result.contains("Test Game"));
//        assertTrue(result.contains("EMPTY")); // Should show empty slots for players
//    }
//
//    // Join Game Tests
//    @Test
//    void joinGame_withValidGameAndColor_shouldSucceed() throws Exception {
//        // Login and create a game
//        AuthData auth = facade.register("testuser", "testpass", "test@email.com");
//        client.setAuthTokenForTest(auth.authToken());
//        int gameId = facade.createGame("Test Game", auth.authToken());
//
//        // List games to populate game number mapping
//        client.evaluateCommand("list games");
//
//        // Join game
//        client.setTestInput("1", "WHITE");  // Assuming it's the first game
//        String result = client.evaluateCommand("join game");
//
//        assertTrue(result.contains("Joined game successfully"));
//    }
//
//    @Test
//    void joinGame_withInvalidGameNumber_shouldFail() throws Exception {
//        // Login first
//        AuthData auth = facade.register("testuser", "testpass", "test@email.com");
//        client.setAuthTokenForTest(auth.authToken());
//
//        client.setTestInput("999", "WHITE");
//        String result = client.evaluateCommand("join game");
//
//        assertTrue(result.contains("Error"));
//    }
//
//    // Observe Game Tests
//    @Test
//    void observeGame_withValidGame_shouldSucceed() throws Exception {
//        // Login and create a game
//        AuthData auth = facade.register("testuser", "testpass", "test@email.com");
//        client.setAuthTokenForTest(auth.authToken());
//        facade.createGame("Test Game", auth.authToken());
//
//        // List games to populate game number mapping
//        client.evaluateCommand("list games");
//
//        // Observe game
//        client.setTestInput("1");  // Assuming it's the first game
//        String result = client.evaluateCommand("observe");
//
//        assertTrue(result.contains("Observing game"));
//    }
//
//    // Invalid Command Tests
//    @Test
//    void invalidCommand_shouldShowError() {
//        String result = client.evaluateCommand("invalid_command");
//        assertTrue(result.contains("Invalid command"));
//    }
//}