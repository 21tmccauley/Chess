package service;

import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.UserData;
import model.AuthData;

public class UserService {
     private final DataAccess dataAccess;
     private final AuthService authService;

    public UserService(DataAccess dataAccess, AuthService authService) {
        this.dataAccess = dataAccess;
        this.authService = authService;
    }
    public AuthData register(UserData user) throws DataAccessException {
        // Check if user already exists
        if (dataAccess.getUser(user.username()) != null) {
            throw new DataAccessException("User already exists");
        }

        // Create user
        dataAccess.createUser(user);

        // Create auth token using AuthService
        return authService.createAuth(user.username());
    }
}
