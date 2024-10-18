package service;

import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthService authService;
    private DataAccess mockDataAccess;

    @BeforeEach
    void setUp() {
        mockDataAccess = mock(DataAccess.class);
        authService = new AuthService(mockDataAccess);
    }

    @Test
    void createAuth() throws DataAccessException {
        String username = "testUser";
        AuthData authData = authService.createAuth(username);

        assertNotNull(authData);
        assertEquals(username, authData.username());
        assertNotNull(authData.authToken());
        verify(mockDataAccess).createAuth(authData);
    }

    @Test
    void getAuth() throws DataAccessException {
        String authToken = "testToken";
        AuthData expectedAuthData = new AuthData(authToken, "testUser");
        when(mockDataAccess.getAuth(authToken)).thenReturn(expectedAuthData);

        AuthData result = authService.getAuth(authToken);

        assertEquals(expectedAuthData, result);
    }

    @Test
    void deleteAuth() throws DataAccessException {
        String authToken = "testToken";
        authService.deleteAuth(authToken);
        verify(mockDataAccess).deleteAuth(authToken);
    }

    @Test
    void generateAuthToken() {
        String token1 = authService.generateAuthToken();
        String token2 = authService.generateAuthToken();

        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
    }
}