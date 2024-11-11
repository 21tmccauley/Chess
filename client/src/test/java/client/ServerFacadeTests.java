package client;

import org.junit.jupiter.api.*;
import server.Server;

public class ServerFacadeTests {
    private static Server server;
    private static ServerFacade facade;
    private static final String VALID_USERNAME = "testUser";
    private static final String VALID_PASSWORD = "testPass";
    private static final String VALID_EMAIL = "test@email.com";

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws Exception {
        facade.clear();
    }

}