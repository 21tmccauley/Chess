package dataaccess;

import model.UserData;
import model.GameData;
import model.AuthData;
import chess.ChessGame;
import com.google.gson.Gson;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class MySQLDataAccess implements Dataaccess {
    private final Gson gson = new Gson();

    public MySQLDataAccess() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        // Create database if it doesn't exist
        DatabaseManager.createDatabase();

        try (var conn = DatabaseManager.getConnection()) {
            // Create tables if they don't exist
            var createStatements = new String[] {
                    """
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(255) NOT NULL PRIMARY KEY,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL
                )
                """,
                    """
                CREATE TABLE IF NOT EXISTS games (
                    gameID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    whiteUsername VARCHAR(255),
                    blackUsername VARCHAR(255),
                    gameName VARCHAR(255) NOT NULL,
                    game TEXT NOT NULL,
                    FOREIGN KEY (whiteUsername) REFERENCES users(username),
                    FOREIGN KEY (blackUsername) REFERENCES users(username)
                )
                """,
                    """
                CREATE TABLE IF NOT EXISTS auth_tokens (
                    authToken VARCHAR(255) NOT NULL PRIMARY KEY,
                    username VARCHAR(255) NOT NULL,
                    FOREIGN KEY (username) REFERENCES users(username)
                )
                """
            };

            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to configure database: %s", e.getMessage()));
        }
    }

    @Override
    public void clearAll() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            // Clear tables in order to respect foreign key constraints
            var clearStatements = new String[] {
                    "DELETE FROM auth_tokens",
                    "DELETE FROM games",
                    "DELETE FROM users"
            };

            for (var statement : clearStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to clear database: %s", e.getMessage()));
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("INSERT INTO users (username, password, email) VALUES (?, ?, ?)")) {
                preparedStatement.setString(1, user.username());
                preparedStatement.setString(2, user.password());
                preparedStatement.setString(3, user.email());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                throw new DataAccessException("User already exists");
            }
            throw new DataAccessException(String.format("Unable to create user: %s", e.getMessage()));
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT * FROM users WHERE username=?")) {
                preparedStatement.setString(1, username);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        return new UserData(
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("email")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to read user: %s", e.getMessage()));
        }
        return null;
    }

    // TODO: Implement remaining interface methods
    @Override
    public int createGame(GameData game) throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public int generateGameId() throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}