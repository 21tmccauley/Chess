package server;

import spark.*;
import com.google.gson.Gson;
import service.*;
import dataAccess.*;
import model.*;

import java.util.Map;

public class Server {
    private final UserService userService;
    private final GameService gameService;
    private final AuthService authService;
    private final DataAccess dataAccess;
    private final Gson gson;

    public Server(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        this.authService = new AuthService(dataAccess);
        this.userService = new UserService(dataAccess, authService);
        this.gameService = new GameService(dataAccess, authService);
        this.gson = new Gson();
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // User endpoints
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);

        // Game endpoints
        Spark.post("/game", this::createGame);
        Spark.get("/game", this::listGames);
        Spark.put("/game", this::joinGame);

        // Clear application endpoint
        Spark.delete("/db", this::clearApplication);

        Spark.exception(DataAccessException.class, this::handleDataAccessException);

        Spark.init();
        Spark.awaitInitialization();

        System.out.println("Server started on port " + Spark.port());
        return Spark.port();
    }

    private Object register(Request req, Response res) {
        var user = gson.fromJson(req.body(), UserData.class);
        try {
            var result = userService.register(user);
            res.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            res.status(403);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    private Object login(Request req, Response res) {
        var loginRequest = gson.fromJson(req.body(), UserData.class);
        try {
            var result = userService.login(loginRequest);
            res.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: unauthorized"));
        }
    }

    private Object logout(Request req, Response res) {
        var authToken = req.headers("Authorization");
        try {
            userService.logout(authToken);
            res.status(200);
            return "{}";
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: unauthorized"));
        }
    }

    private Object createGame(Request req, Response res) {
        var authToken = req.headers("Authorization");
        var createGameRequest = gson.fromJson(req.body(), Map.class);
        var gameName = (String) createGameRequest.get("gameName");
        try {
            int gameId = gameService.createGame(authToken, gameName);
            res.status(200);
            return gson.toJson(Map.of("gameID", gameId));
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: unauthorized"));
        }
    }

    private Object listGames(Request req, Response res) {
        var authToken = req.headers("Authorization");
        try {
            var games = gameService.listGames(authToken);
            res.status(200);
            return gson.toJson(Map.of("games", games));
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: unauthorized"));
        }
    }

    private Object joinGame(Request req, Response res) {
        var authToken = req.headers("Authorization");
        var joinGameRequest = gson.fromJson(req.body(), Map.class);
        var gameId = ((Double) joinGameRequest.get("gameID")).intValue();
        var playerColor = (String) joinGameRequest.get("playerColor");
        try {
            gameService.joinGame(authToken, gameId, playerColor);
            res.status(200);
            return "{}";
        } catch (DataAccessException e) {
            res.status(403);
            return gson.toJson(Map.of("message", "Error: already taken"));
        }
    }

    private Object clearApplication(Request req, Response res) {
        try {
            dataAccess.clearAll();
            res.status(200);
            return "{}";
        } catch (DataAccessException e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    private void handleDataAccessException(DataAccessException e, Request req, Response res) {
        res.status(500);
        res.body(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}