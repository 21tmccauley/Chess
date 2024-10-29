package service;

import dataaccess.Dataaccess;
import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    private final Dataaccess dataAccess;
    private final AuthService authService;

    public UserService(Dataaccess dataAccess, AuthService authService) {
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

    public AuthData login(UserData user) throws DataAccessException {
        // Get the stored user data from the database
        UserData storedUser = dataAccess.getUser(user.username());

        // Check if user exists and password matches using BCrypt
        if (storedUser == null || !BCrypt.checkpw(user.password(), storedUser.password())) {
            throw new DataAccessException("Invalid username or password");
        }

        // If credentials are valid, create new auth token
        return authService.createAuth(user.username());
    }

    public void logout(String authToken) throws DataAccessException {
        // Verify the auth token
        AuthData authData = authService.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Invalid auth token");
        }

        // Delete the auth token
        authService.deleteAuth(authToken);
    }
}