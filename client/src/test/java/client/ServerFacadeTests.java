package client;

import org.junit.jupiter.api.*;
import server.Server;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

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
}