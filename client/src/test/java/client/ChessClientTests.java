package client;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import server.Server;

public class ChessClientTests {
    private static Server server;
    private static int port;
    private static ServerFacade facade;
    private ChessClient client;  // Changed to instance variable instead of static

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void setup() throws Exception {
        facade.clear();
        client = new ChessClient(port);  // Create new client for each test
    }

    @Test
    void initialState_shouldBePreLogin() {
        assertEquals(ChessClient.State.PRELOGIN, client.getState());
    }

    @Test
    void login_successfulLogin_shouldChangeState() throws Exception {
        // Register a test user first
        facade.register("testuser", "testpass", "test@email.com");

        // Test the login command with valid credentials
        client.setTestInput("testuser", "testpass");
        String result = client.evaluateCommand("login");

        assertTrue(result.contains("Logged in successfully"));
        assertEquals(ChessClient.State.POSTLOGIN, client.getState());
        assertNotNull(client.getAuthToken());
    }

    @Test
    void login_failedLogin_shouldStayInPreLoginState() throws Exception {
        // Test with non-existent user
        client.setTestInput("wronguser", "wrongpass");
        String result = client.evaluateCommand("login");

        assertTrue(result.contains("Error"));
        assertEquals(ChessClient.State.PRELOGIN, client.getState());
        assertNull(client.getAuthToken());
    }

    @Test
    void login_emptyCredentials_shouldReturnError() throws Exception {
        client.setTestInput("", "");
        String result = client.evaluateCommand("login");

        assertTrue(result.contains("Error"));
        assertEquals(ChessClient.State.PRELOGIN, client.getState());
        assertNull(client.getAuthToken());
    }

    @Test
    void evaluateCommand_helpCommand_returnsHelpString() {
        String result = client.evaluateCommand("help");
        assertTrue(result.contains("Available commands"));
        assertTrue(result.contains("login"));
        assertTrue(result.contains("register"));
        assertTrue(result.contains("quit"));
    }

    @Test
    void evaluateCommand_invalidCommand_returnsError() {
        String result = client.evaluateCommand("invalidcommand");
        assertTrue(result.contains("Invalid command"));
    }
}