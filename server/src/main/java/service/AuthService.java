package service;

import dataaccess.Dataaccess;
import dataaccess.DataAccessException;
import model.AuthData;

public class AuthService {
    private final Dataaccess dataAccess;

    public AuthService(Dataaccess dataAccess) {
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

    public void deleteAuth(String authToken) throws DataAccessException {
        dataAccess.deleteAuth(authToken);
    }
}