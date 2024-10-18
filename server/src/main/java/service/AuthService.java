package service;

import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthData;

public class AuthService {
    private final DataAccess dataAccess;

    public AuthService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public String generateAuthToken() {
        return java.util.UUID.randomUUID().toString();
    }

    public AuthData createAuth(String username) throws DataAccessException {
        String authToken = generateAuthToken();
        AuthData authData = new AuthData(authToken, username);
        dataAccess.createAuth(authData);
        return authData;
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        return dataAccess.getAuth(authToken);
    }
}