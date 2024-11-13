package client;

import model.AuthData;
import java.util.Scanner;

public class ChessClient {
    private final ServerFacade server;
    private State state;
    private String authToken;
    private String[] testInput;  // For testing
    private int testInputIndex;  // For testing
    private final Scanner scanner;

    public ChessClient(int port) {
        this.server = new ServerFacade(port);
        this.state = State.PRELOGIN;
        this.scanner = new Scanner(System.in);
    }

    // Helper method for testing
    public void setTestInput(String... inputs) {
        this.testInput = inputs;
        this.testInputIndex = 0;
    }

    private String getInput() {
        if (testInput != null && testInputIndex < testInput.length) {
            return testInput[testInputIndex++];
        }
        return scanner.nextLine();
    }

    public State getState() {
        return state;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String evaluateCommand(String command) {
        try {
            return switch (command.toLowerCase()) {
                case "help" -> state == State.PRELOGIN ? getPreLoginHelp() : getPostLoginHelp();
                case "login" -> handleLogin();
                default -> "Invalid command. Type 'help' for a list of commands.";
            };
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String handleLogin() throws Exception {
        System.out.print("Username: ");
        String username = getInput().trim();
        System.out.print("Password: ");
        String password = getInput().trim();

        if (username.isEmpty() || password.isEmpty()) {
            return "Error: Username and password cannot be empty";
        }

        try {
            AuthData authData = server.login(username, password);
            this.authToken = authData.authToken();
            this.state = State.POSTLOGIN;
            return "Logged in successfully as " + username;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String getPreLoginHelp() {
        return """
            Available commands:
            - help: Display this help message
            - login: Login with existing account
            - register: Create new account
            - quit: Exit the program
            """;
    }

    private String getPostLoginHelp() {
        return """
            Available commands:
            - help: Display this help message
            - logout: Logout from current session
            - create game: Create a new chess game
            - list games: List all available games
            - join game: Join an existing game
            - observe: Observe an existing game
            """;
    }

    public enum State {
        PRELOGIN,
        POSTLOGIN
    }
}