package dataaccess;

import model.UserData;
import model.GameData;
import model.AuthData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccesss implements DataAccess {
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private final Map<String, AuthData> auths = new HashMap<>();

    private int nextGameId = 1;

    private int generateGameId(){
        return nextGameId++;
    }

    public void clear(){
        users.clear();
        games.clear();
        auths.clear();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new DataAccessException("User already exists");
        }
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        UserData user = users.get(username);
        if (user == null) {
            throw new DataAccessException("User not found");
        }
        return user;
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        if (games.containsKey(game.gameID())) {
            throw new DataAccessException("Game already exists");
        }
        games.put(game.gameID(), game);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        GameData game = games.get(gameID);
        if (game == null) {
            throw new DataAccessException("Game not found");
        }
        return game;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (!games.containsKey(game.gameID())) {
            throw new DataAccessException("Game not found");
        }
        games.put(game.gameID(), game);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(games.values());
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        if (auths.containsKey(auth.authToken())) {
            throw new DataAccessException("Auth token already exists");
        }
        auths.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        AuthData auth = auths.get(authToken);
        if (auth == null) {
            throw new DataAccessException("Auth token not found");
        }
        return auth;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (!auths.containsKey(authToken)) {
            throw new DataAccessException("Auth token not found");
        }
        auths.remove(authToken);
    }
}
