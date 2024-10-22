package service;

import dataaccess.MemoryDataaccess;
import dataaccess.Dataaccess;
import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private UserService userService;
    private Dataaccess dataAccess;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Use a real in-memory implementation instead of mocks
        dataAccess = new MemoryDataaccess();
        authService = new AuthService(dataAccess);
        userService = new UserService(dataAccess, authService);

        // Clear any existing data before each test
        try {
            dataAccess.clearAll();
        } catch (DataAccessException e) {
            fail("Failed to clear database before test");
        }
    }

    @Test
    void registerNewUserSuccess() throws DataAccessException {
        // Arrange
        UserData userData = new UserData("newUser", "password", "email@example.com");

        // Act
        AuthData result = userService.register(userData);

        // Assert
        assertNotNull(result);
        assertEquals("newUser", result.username());
        assertNotNull(result.authToken());

        // Verify user was actually created in the database
        UserData storedUser = dataAccess.getUser("newUser");
        assertNotNull(storedUser);
        assertEquals(userData.username(), storedUser.username());
        assertEquals(userData.password(), storedUser.password());
        assertEquals(userData.email(), storedUser.email());
    }

    @Test
    void registerExistingUserFails() throws DataAccessException {
        // Arrange
        UserData userData = new UserData("existingUser", "password", "email@example.com");

        // Create the user first
        userService.register(userData);

        // Act & Assert
        assertThrows(DataAccessException.class, () -> userService.register(userData));
    }

    @Test
    void loginValidUserSuccess() throws DataAccessException {
        // Arrange
        UserData userData = new UserData("validUser", "password", "email@example.com");
        userService.register(userData); // Register user first

        // Act
        AuthData result = userService.login(userData);

        // Assert
        assertNotNull(result);
        assertEquals("validUser", result.username());
        assertNotNull(result.authToken());

        // Verify auth token was actually created
        AuthData storedAuth = dataAccess.getAuth(result.authToken());
        assertNotNull(storedAuth);
        assertEquals(result.authToken(), storedAuth.authToken());
    }

    @Test
    void loginInvalidUserFails() {
        // Arrange
        UserData userData = new UserData("invalidUser", "password", "email@example.com");

        // Act & Assert
        assertThrows(DataAccessException.class, () -> userService.login(userData));
    }

    @Test
    void loginWrongPasswordFails() throws DataAccessException {
        // Arrange
        UserData correctUser = new UserData("user", "correctPassword", "email@example.com");
        userService.register(correctUser); // Register with correct password

        UserData wrongPassUser = new UserData("user", "wrongPassword", "email@example.com");

        // Act & Assert
        assertThrows(DataAccessException.class, () -> userService.login(wrongPassUser));
    }

    @Test
    void logoutValidTokenSuccess() throws DataAccessException {
        // Arrange
        UserData userData = new UserData("user", "password", "email@example.com");
        AuthData authData = userService.register(userData);

        // Act
        userService.logout(authData.authToken());

        // Assert
        // Verify the auth token was actually deleted by expecting an exception
        assertThrows(DataAccessException.class, () -> dataAccess.getAuth(authData.authToken()));
    }

    @Test
    void logoutInvalidTokenFails() {
        // Arrange
        String invalidToken = "invalidToken";

        // Act & Assert
        assertThrows(DataAccessException.class, () -> userService.logout(invalidToken));
    }
}