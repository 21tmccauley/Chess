package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.GameData;
import model.AuthData;

import java.util.Collection;

public class GameService {
    private final DataAccess dataAccess;
    private final AuthService authService;

    public GameService(DataAccess dataAccess, AuthService authService){
        this.dataAccess = dataAccess;
        this.authService = authService;
    }


}
