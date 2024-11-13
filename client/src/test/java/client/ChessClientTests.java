package client;

import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

public class ChessClientTests {
    private static Server server;
    private static ChessClient client;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
        client = new ChessClient(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws Exception {
        facade.clear();
    }

    @Test
    void initialState_shouldBePreLogin() {
        assertEquals(ChessClient.State.PRELOGIN, client.getState());
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