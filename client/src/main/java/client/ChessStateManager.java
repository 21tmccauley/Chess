package client;

import chess.ChessGame;
import chess.ChessPiece;
import model.AuthData;
import model.GameData;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.util.HashMap;
import java.util.Map;

public class ChessStateManager implements MessageHandler {
    private State currentState = State.PRELOGIN;
    private final ServerFacade server;
    private final UIManager uiManager;
    private final GameManager gameManager;
    private final CommandProcessor commandProcessor;
    private final ConnectionManager connectionManager;

    private String authToken;
    private final Map<Integer, Integer> gameNumberToId = new HashMap<>();

    public ChessStateManager(int port) {
        String serverUrl = "http://localhost:" + port;
        this.server = new ServerFacade(port);
        this.uiManager = new UIManager();
        this.connectionManager = new ConnectionManager(serverUrl, this);
        this.gameManager = new GameManager(connectionManager);
        this.commandProcessor = new CommandProcessor(gameManager, uiManager, connectionManager);
    }

    public String handleCommand(String input) {
        try {
            switch (currentState) {
                case PRELOGIN -> {
                    return handlePreLoginCommand(input);
                }
                case POSTLOGIN -> {
                    return handlePostLoginCommand(input);
                }
                case GAMEPLAY -> {
                    if (input.equalsIgnoreCase("help")) {
                        // Check if we still have a valid game ID
                        if (gameManager.getCurrentGameId() != null) {
                            commandProcessor.processCommand(input);
                        } else {
                            // No active game - reset to post-login state
                            currentState = State.POSTLOGIN;
                            return handlePostLoginCommand(input);
                        }
                    } else {
                        commandProcessor.processCommand(input);
                    }
                    return null;
                }
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        return "Invalid command";
    }

    private String handlePreLoginCommand(String command) throws Exception {
        return switch (command.toLowerCase()) {
            case "help" -> getPreLoginHelp();
            case "login" -> handleLogin();
            case "register" -> handleRegister();
            case "quit" -> {
                System.exit(0);
                yield "Goodbye!";
            }
            default -> "Invalid command. Type 'help' for a list of commands.";
        };
    }

    private String handlePostLoginCommand(String command) throws Exception {
        return switch (command.toLowerCase()) {
            case "help" -> getPostLoginHelp();
            case "logout" -> handleLogout();
            case "create game" -> handleCreateGame();
            case "list games" -> handleListGames();
            case "join game" -> handleJoinGame();
            case "observe" -> handleObserveGame();
            case "quit" -> {
                System.exit(0);
                yield "Goodbye!";
            }
            default -> "Invalid command. Type 'help' for a list of commands.";
        };
    }

    private String handleLogin() throws Exception {
        String username = uiManager.promptForInput("Username: ");
        String password = uiManager.promptForInput("Password: ");

        AuthData authData = server.login(username, password);
        setLoggedIn(authData.authToken());
        return "Logged in successfully as " + username;
    }

    private String handleRegister() throws Exception {
        String username = uiManager.promptForInput("Username: ");
        String password = uiManager.promptForInput("Password: ");
        String email = uiManager.promptForInput("Email: ");

        AuthData authData = server.register(username, password, email);
        setLoggedIn(authData.authToken());
        return "Registration successful. Welcome " + username + "!";
    }

    private String handleLogout() throws Exception {
        server.logout(authToken);
        setLoggedOut();
        return "Logged out successfully";
    }

    private String handleCreateGame() throws Exception {
        String gameName = uiManager.promptForInput("Enter game name: ");
        server.createGame(gameName, authToken);
        return "Game created successfully";
    }

    private String handleListGames() throws Exception {
        var games = server.listGames(authToken);
        return formatGamesList(games);
    }

    private String handleJoinGame() throws Exception {
        String gameList = handleListGames();
        uiManager.displayMessage(gameList);

        int gameNumber = Integer.parseInt(uiManager.promptForInput("Enter game number: "));
        if (!gameNumberToId.containsKey(gameNumber)) {
            return "Invalid game number";
        }

        String color = uiManager.promptForInput("Choose team color (WHITE/BLACK/[ENTER] for observe): ")
                .trim().toUpperCase();

        int gameId = gameNumberToId.get(gameNumber);

        try {
            // Set up game state
            if (color.isEmpty()) {
                System.out.println("Debug: Setting up observer mode for game " + gameId);
                gameManager.setCurrentGame(gameId, null);
            } else {
                if (!color.equals("WHITE") && !color.equals("BLACK")) {
                    return "Error: Invalid player color. Must be WHITE or BLACK";
                }
                server.joinGame(authToken, gameId, color);
                gameManager.setCurrentGame(gameId, color);
            }

            System.out.println("Debug: Attempting to connect to game " + gameId);

            try {
                connectionManager.connectToGame(gameId);
                currentState = State.GAMEPLAY;
                return String.format("Joined game %d as %s",
                        gameNumber,
                        color.isEmpty() ? "an observer" : "the " + color + " player");
            } catch (Exception e) {
                // Connection failed - reset game state
                gameManager.clearGameState();
                currentState = State.POSTLOGIN;
                throw e;
            }
        } catch (Exception e) {
            System.out.println("Debug: Error during game connection: " + e.getMessage());
            return "Error connecting to game: " + e.getMessage();
        }
    }

    private String handleObserveGame() throws Exception {
        return handleJoinGame();
    }

    private void setLoggedIn(String authToken) {
        this.authToken = authToken;
        this.connectionManager.setAuthToken(authToken);
        this.currentState = State.POSTLOGIN;
    }

    private void setLoggedOut() {
        this.authToken = null;
        this.connectionManager.setAuthToken(null);
        this.currentState = State.PRELOGIN;
    }

    private String getPreLoginHelp() {
        return """
            Available commands:
            - help: Display help message
            - login: Login with existing account
            - register: Create new account
            - quit: Exit the program
            """;
    }

    private String getPostLoginHelp() {
        return """
            Available commands:
            - help: Display help message
            - logout: Logout from current session
            - create game: Create a new chess game
            - list games: List all available games
            - join game: Join an existing game
            - observe: Observe an existing game
            """;
    }

    private String formatGamesList(java.util.Collection<GameData> games) {
        gameNumberToId.clear();
        StringBuilder result = new StringBuilder("\nAvailable Games:\n");
        int gameNumber = 1;

        for (GameData game : games) {
            gameNumberToId.put(gameNumber, game.gameID());
            result.append(String.format("%d. %s (White: %s, Black: %s)%n",
                    gameNumber,
                    game.gameName(),
                    game.whiteUsername() != null ? game.whiteUsername() : "EMPTY",
                    game.blackUsername() != null ? game.blackUsername() : "EMPTY"));
            gameNumber++;
        }

        return result.toString();
    }

    @Override
    public void handleGameUpdate(LoadGameMessage message) {
        gameManager.updateGameState(message);
        uiManager.clearScreen();
        uiManager.drawChessBoard(message.getGame());
    }

    @Override
    public void handleNotification(NotificationMessage message) {
        uiManager.displayNotification(message.getMessage());
    }

    @Override
    public void handleError(String errorMessage) {
        uiManager.displayError(errorMessage);
    }

    public enum State {
        PRELOGIN,
        POSTLOGIN,
        GAMEPLAY
    }

    public State getCurrentState() {
        return currentState;
    }
}