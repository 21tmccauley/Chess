package dataaccess;

import model.UserData;
import model.GameData;
import model.AuthData;
import chess.ChessGame;
import com.google.gson.Gson;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class MySQLDataAccess implements Dataaccess {
    private final Gson gson = new Gson();

    public MySQLDataAccess() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();

        try (var conn = DatabaseManager.getConnection()) {
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
                    FOREIGN KEY (whiteUsername) REFERENCES users(username) ON DELETE SET NULL,
                    FOREIGN KEY (blackUsername) REFERENCES users(username) ON DELETE SET NULL
                )
                """,
                    """
                CREATE TABLE IF NOT EXISTS auth_tokens (
                    authToken VARCHAR(255) NOT NULL PRIMARY KEY,
                    username VARCHAR(255) NOT NULL,
                    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
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
            // Disable foreign key checks
            try (var stmt = conn.prepareStatement("SET FOREIGN_KEY_CHECKS=0")) {
                stmt.executeUpdate();
            }

            var clearStatements = new String[] {
                    "TRUNCATE TABLE auth_tokens",
                    "TRUNCATE TABLE games",
                    "TRUNCATE TABLE users"
            };

            for (var statement : clearStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }

            // Re-enable foreign key checks
            try (var stmt = conn.prepareStatement("SET FOREIGN_KEY_CHECKS=1")) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to clear database: %s", e.getMessage()));
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(
                    "INSERT INTO users (username, password, email) VALUES (?, ?, ?)")) {
                String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
                preparedStatement.setString(1, user.username());
                preparedStatement.setString(2, hashedPassword);
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

    @Override
    public int createGame(GameData game) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(
                    "INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, game.whiteUsername());
                preparedStatement.setString(2, game.blackUsername());
                preparedStatement.setString(3, game.gameName());
                preparedStatement.setString(4, gson.toJson(game.game()));

                preparedStatement.executeUpdate();

                try (var generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    } else {
                        throw new DataAccessException("Creating game failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to create game: %s", e.getMessage()));
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT * FROM games WHERE gameID=?")) {
                preparedStatement.setInt(1, gameID);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        return new GameData(
                                rs.getInt("gameID"),
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                rs.getString("gameName"),
                                gson.fromJson(rs.getString("game"), ChessGame.class)
                        );
                    }
                    throw new DataAccessException("Game not found");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to read game: %s", e.getMessage()));
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            String sql = "UPDATE games SET whiteUsername=?, blackUsername=?, gameName=?, game=? WHERE gameID=?";
            try (var preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setString(1, game.whiteUsername());
                preparedStatement.setString(2, game.blackUsername());
                preparedStatement.setString(3, game.gameName());
                preparedStatement.setString(4, gson.toJson(game.game()));
                preparedStatement.setInt(5, game.gameID());

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected == 0) {
                    throw new DataAccessException("Game not found");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to update game: %s", e.getMessage()));
        }
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(
                    "INSERT INTO auth_tokens (authToken, username) VALUES (?, ?)")) {
                preparedStatement.setString(1, auth.authToken());
                preparedStatement.setString(2, auth.username());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                throw new DataAccessException("Auth token already exists");
            }
            throw new DataAccessException(String.format("Unable to create auth token: %s", e.getMessage()));
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT * FROM auth_tokens WHERE authToken=?")) {
                preparedStatement.setString(1, authToken);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(
                                rs.getString("authToken"),
                                rs.getString("username")
                        );
                    }
                    throw new DataAccessException("Auth token not found");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to read auth token: %s", e.getMessage()));
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("DELETE FROM auth_tokens WHERE authToken=?")) {
                preparedStatement.setString(1, authToken);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected == 0) {
                    throw new DataAccessException("Auth token not found");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to delete auth token: %s", e.getMessage()));
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT * FROM games")) {
                try (var rs = preparedStatement.executeQuery()) {
                    Collection<GameData> games = new ArrayList<>();
                    while (rs.next()) {
                        games.add(new GameData(
                                rs.getInt("gameID"),
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                rs.getString("gameName"),
                                gson.fromJson(rs.getString("game"), ChessGame.class)
                        ));
                    }
                    return games;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to list games: %s", e.getMessage()));
        }
    }

    @Override
    public int generateGameId() throws DataAccessException {
        return createGame(new GameData(0, null, null, "", new ChessGame()));
    }
}