package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;

public class AuthService {
    private final DataAccess dataAccess;

    public AuthService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public String generateAuthToken() {
        String authToken = java.util.UUID.randomUUID().toString();
        // You might want to check if this token already exists in your database
        return authToken;
    }

    public AuthData createAuth(String username) throws DataAccessException {
        String authToken = generateAuthToken();
        AuthData authData = new AuthData(authToken, username);
        dataAccess.createAuth(authData);
        return authData;  // Return the created AuthData
    }

    // Other auth-related methods...
}