package service;

import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.UserData;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserService userService;
    private DataAccess mockDataAccess;
    private AuthService mockAuthService;

    @BeforeEach
    void setUp() {
        mockDataAccess = mock(DataAccess.class);
        mockAuthService = mock(AuthService.class);
        userService = new UserService(mockDataAccess, mockAuthService);
    }

    @Test
    void registerNewUser() throws DataAccessException {
        UserData userData = new UserData("newUser", "password", "email@example.com");
        AuthData authData = new AuthData("token123", "newUser");

        when(mockDataAccess.getUser("newUser")).thenReturn(null);
        when(mockAuthService.createAuth("newUser")).thenReturn(authData);

        AuthData result = userService.register(userData);

        verify(mockDataAccess).createUser(userData);
        assertEquals(authData, result);
    }

    @Test
    void registerExistingUser() throws DataAccessException {
        UserData userData = new UserData("existingUser", "password", "email@example.com");

        when(mockDataAccess.getUser("existingUser")).thenReturn(userData);

        assertThrows(DataAccessException.class, () -> userService.register(userData));
    }

    @Test
    void loginValidUser() throws DataAccessException {
        UserData userData = new UserData("validUser", "password", "email@example.com");
        AuthData authData = new AuthData("token123", "validUser");

        when(mockDataAccess.getUser("validUser")).thenReturn(userData);
        when(mockAuthService.createAuth("validUser")).thenReturn(authData);

        AuthData result = userService.login(userData);

        assertEquals(authData, result);
    }

    @Test
    void loginInvalidUser() throws DataAccessException {
        UserData userData = new UserData("invalidUser", "password", "email@example.com");

        when(mockDataAccess.getUser("invalidUser")).thenReturn(null);

        assertThrows(DataAccessException.class, () -> userService.login(userData));
    }

    @Test
    void loginWrongPassword() throws DataAccessException {
        UserData userData = new UserData("user", "wrongPassword", "email@example.com");
        UserData storedUser = new UserData("user", "correctPassword", "email@example.com");

        when(mockDataAccess.getUser("user")).thenReturn(storedUser);

        assertThrows(DataAccessException.class, () -> userService.login(userData));
    }

    @Test
    void logoutValidToken() throws DataAccessException {
        String validToken = "validToken";
        AuthData authData = new AuthData(validToken, "user");

        when(mockAuthService.getAuth(validToken)).thenReturn(authData);

        assertDoesNotThrow(() -> userService.logout(validToken));
        verify(mockAuthService).deleteAuth(validToken);
    }

    @Test
    void logoutInvalidToken() throws DataAccessException {
        String invalidToken = "invalidToken";

        when(mockAuthService.getAuth(invalidToken)).thenReturn(null);

        assertThrows(DataAccessException.class, () -> userService.logout(invalidToken));
    }
}