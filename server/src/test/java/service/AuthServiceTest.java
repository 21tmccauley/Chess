package service;

import dataAccess.MemoryDataaccess;
import dataAccess.Dataaccess;
import dataAccess.DataAccessException;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {
    private AuthService authService;
    private Dataaccess dataAccess;

    @BeforeEach
    void setUp() {
        // Use real implementation with in-memory data store
        dataAccess = new MemoryDataaccess();
        authService = new AuthService(dataAccess);

        // Clear data before each test
        try {
            dataAccess.clearAll();
        } catch (DataAccessException e) {
            fail("Failed to clear database before test");
        }
    }

    @Test
    void createAuthSuccess() throws DataAccessException {
        // Arrange
        String username = "testUser";

        // Act
        AuthData authData = authService.createAuth(username);

        // Assert
        assertNotNull(authData);
        assertEquals(username, authData.username());
        assertNotNull(authData.authToken());

        // Verify auth was actually created in the database
        AuthData storedAuth = dataAccess.getAuth(authData.authToken());
        assertNotNull(storedAuth);
        assertEquals(username, storedAuth.username());
        assertEquals(authData.authToken(), storedAuth.authToken());
    }

    @Test
    void createAuthMultipleTokensUnique() throws DataAccessException {
        // Arrange
        String username = "testUser";

        // Act
        AuthData auth1 = authService.createAuth(username);
        AuthData auth2 = authService.createAuth(username);

        // Assert
        assertNotNull(auth1);
        assertNotNull(auth2);
        assertNotEquals(auth1.authToken(), auth2.authToken());

        // Verify both auths exist in the database
        AuthData storedAuth1 = dataAccess.getAuth(auth1.authToken());
        AuthData storedAuth2 = dataAccess.getAuth(auth2.authToken());
        assertNotNull(storedAuth1);
        assertNotNull(storedAuth2);
        assertEquals(username, storedAuth1.username());
        assertEquals(username, storedAuth2.username());
    }

    @Test
    void getAuthExistingAuthSuccess() throws DataAccessException {
        // Arrange
        String username = "testUser";
        AuthData createdAuth = authService.createAuth(username);

        // Act
        AuthData retrievedAuth = authService.getAuth(createdAuth.authToken());

        // Assert
        assertNotNull(retrievedAuth);
        assertEquals(createdAuth.username(), retrievedAuth.username());
        assertEquals(createdAuth.authToken(), retrievedAuth.authToken());
    }

    @Test
    void getAuthNonexistentAuthThrowsException() {
        // Arrange
        String nonexistentToken = "nonexistentToken";

        // Act & Assert
        // Should throw exception for non-existent auth token
        assertThrows(DataAccessException.class, () -> authService.getAuth(nonexistentToken));
    }

    @Test
    void deleteAuthExistingAuthSuccess() throws DataAccessException {
        // Arrange
        String username = "testUser";
        AuthData authData = authService.createAuth(username);

        // Act
        authService.deleteAuth(authData.authToken());

        // Assert
        // Verify auth was actually deleted from the database
        assertThrows(DataAccessException.class, () -> dataAccess.getAuth(authData.authToken()));
    }

    @Test
    void deleteAuthNonexistentAuthThrowsException() {
        // Arrange
        String nonexistentToken = "nonexistentToken";

        // Act & Assert
        // Should throw exception when deleting non-existent auth
        assertThrows(DataAccessException.class, () -> authService.deleteAuth(nonexistentToken));
    }

    @Test
    void generateAuthTokenUniqueTokens() {
        // Act
        String token1 = authService.generateAuthToken();
        String token2 = authService.generateAuthToken();
        String token3 = authService.generateAuthToken();

        // Assert
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotNull(token3);

        // Verify all tokens are different
        assertNotEquals(token1, token2);
        assertNotEquals(token2, token3);
        assertNotEquals(token1, token3);

        // Verify tokens are not empty
        assertFalse(token1.isEmpty());
        assertFalse(token2.isEmpty());
        assertFalse(token3.isEmpty());
    }

    @Test
    void generateAuthTokenValidFormat() {
        // Act
        String token = authService.generateAuthToken();

        // Assert
        assertNotNull(token);
        assertTrue(token.length() >= 16, "Token should be at least 16 characters long");
    }
}