package server;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import server.websocket.WebSocketHandle;
import spark.*;
import com.google.gson.Gson;
import service.*;
import dataaccess.*;
import model.*;
import spark.utils.Assert;
import java.util.Map;

public class Server {
    private Dataaccess dataAccess;
    private AuthService authService;
    private UserService userService;
    private GameService gameService;
    private Gson gson;

    public Server() {
        // Default constructor
    }

    public void initializeDataAccess(Dataaccess dataAccess) {
        this.dataAccess = dataAccess;
        this.gson = new Gson();
        this.authService = new AuthService(dataAccess);
        this.userService = new UserService(dataAccess, authService);
        this.gameService = new GameService(dataAccess, authService);
    }

    public int run(int desiredPort) {
        if (dataAccess == null) {
            try {
                initializeDataAccess(new MySQLDataAccess());
            } catch (DataAccessException e) {
                throw new RuntimeException(e);
            }
        }

        Spark.port(desiredPort);

        // WebSocket must be configured before anything else
        Spark.webSocket("/ws", new WebSocketHandle(dataAccess));

        Spark.staticFiles.location("resources/web");

        // CORS and other configurations
        Spark.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        });

        registerEndpoints();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private void registerEndpoints() {
        // WebSocket endpoint
//        Spark.webSocket("/ws", WebSocketHandle.class);

        // User endpoints
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);

        // Game endpoints
        Spark.post("/game", this::createGame);
        Spark.get("/game", this::listGames);
        Spark.put("/game", this::joinGame);

        // Admin endpoints
        Spark.delete("/db", this::clearApplication);


    }
    private Object register(Request req, Response res) {
        try {
            var user = gson.fromJson(req.body(), UserData.class);
            if (user == null || isEmpty(user.username()) || isEmpty(user.password()) || isEmpty(user.email())) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            var result = userService.register(user);
            res.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            if (e.getMessage().contains("User already exists")) {
                res.status(403);
                return gson.toJson(Map.of("message", "Error: already taken"));
            }
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    private Object login(Request req, Response res) {
        try {
            var loginRequest = gson.fromJson(req.body(), UserData.class);
            if (loginRequest == null || isEmpty(loginRequest.username()) || isEmpty(loginRequest.password())) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            var result = userService.login(loginRequest);
            res.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: unauthorized"));
        }
    }

    private Object logout(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            if (isEmpty(authToken)) {
                res.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            userService.logout(authToken);
            res.status(200);
            return "{}";
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: unauthorized"));
        }
    }

    private Object createGame(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            if (isEmpty(authToken)) {
                res.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            var createGameRequest = gson.fromJson(req.body(), Map.class);
            String gameName = (String) createGameRequest.get("gameName");
            if (isEmpty(gameName)) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            try {
                int gameId = gameService.createGame(authToken, gameName);
                res.status(200);
                return gson.toJson(Map.of("gameID", gameId));
            } catch (DataAccessException e) {
                if (e.getMessage().contains("Invalid auth token") ||
                        e.getMessage().contains("Auth token not found")) {
                    res.status(401);
                    return gson.toJson(Map.of("message", "Error: unauthorized"));
                }
                throw e;  // rethrow other DataAccessExceptions to be handled as 500
            }
        } catch (DataAccessException e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    private Object listGames(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            if (isEmpty(authToken)) {
                res.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            var games = gameService.listGames(authToken);
            res.status(200);
            return gson.toJson(Map.of("games", games));
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: unauthorized"));
        }
    }

    private Object joinGame(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            if (isEmpty(authToken)) {
                res.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            var joinGameRequest = gson.fromJson(req.body(), Map.class);
            if (joinGameRequest == null || !joinGameRequest.containsKey("gameID")) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            int gameId = ((Double) joinGameRequest.get("gameID")).intValue();
            String playerColor = (String) joinGameRequest.get("playerColor");

            try {
                gameService.joinGame(authToken, gameId, playerColor);
                res.status(200);
                return "{}";
            } catch (DataAccessException e) {
                if (e.getMessage().contains("Invalid auth token") ||
                        e.getMessage().contains("Auth token not found")) {
                    res.status(401);
                    return gson.toJson(Map.of("message", "Error: unauthorized"));
                } else if (e.getMessage().contains("already joined")) {
                    res.status(403);
                    return gson.toJson(Map.of("message", "Error: already taken"));
                } else if (e.getMessage().contains("Game not found") ||
                        e.getMessage().contains("Invalid player color")) {
                    res.status(400);
                    return gson.toJson(Map.of("message", "Error: bad request"));
                }
                throw e;  // rethrow other DataAccessExceptions to be handled as 500
            }
        } catch (DataAccessException e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
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

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}