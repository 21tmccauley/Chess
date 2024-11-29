package client;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import ui.EscapeSequences;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ChessClient {
    private final ServerFacade server;
    private State state;
    private String authToken;
    private final Scanner scanner;
    private final Map<Integer, Integer> gameNumberToId;

    public ChessClient(int port) {
        this.server = new ServerFacade(port);
        this.state = State.PRELOGIN;
        this.scanner = new Scanner(System.in);
        this.gameNumberToId = new HashMap<>();
    }

    public State getState() {
        return state;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String evaluateCommand(String command) {
        try {
            if (state == State.PRELOGIN) {
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
            } else {
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
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String handleLogin() throws Exception {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

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

    private String handleRegister() throws Exception {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            return "Error: All fields are required";
        }

        try {
            AuthData authData = server.register(username, password, email);
            this.authToken = authData.authToken();
            this.state = State.POSTLOGIN;
            return "Registration successful. Welcome " + username + "!";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String handleLogout() throws Exception {
        try {
            server.logout(authToken);
            this.authToken = null;
            this.state = State.PRELOGIN;
            return "Logged out successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String handleCreateGame() throws Exception {
        System.out.print("Enter game name: ");
        String gameName = scanner.nextLine().trim();

        if (gameName.isEmpty()) {
            return "Error: Game name cannot be empty";
        }

        try {
            int gameId = server.createGame(gameName, authToken);
            return "Game created successfully ";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String handleListGames() throws Exception {
        try {
            Collection<GameData> games = server.listGames(authToken);
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
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String handleJoinGame() throws Exception {
        if (state != State.POSTLOGIN) {
            return "Please login first";
        }

        System.out.print("Enter game number: ");
        int gameNumber = Integer.parseInt(scanner.nextLine().trim());

        if (!gameNumberToId.containsKey(gameNumber)) {
            return "Invalid game number";
        }

        System.out.print("Choose team color (WHITE/BLACK/[ENTER] for observe): ");
        String color = scanner.nextLine().trim().toUpperCase();

        // Only pass color if it's WHITE or BLACK
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            color = null; // This will make them an observer
        }

        try {
            int gameId = gameNumberToId.get(gameNumber);

            // Add debug logging
//            System.out.println("DEBUG: Attempting to join game " + gameId + " as " + (color != null ? color : "observer"));

            server.joinGame(authToken, gameId, color);

            // Verify the join was successful by fetching the updated game list
            Collection<GameData> games = server.listGames(authToken);
            boolean joinSuccessful = false;

            for (GameData game : games) {
                if (game.gameID() == gameId) {
                    if (color != null) {
                        if (color.equals("WHITE") && game.whiteUsername() != null) {
                            joinSuccessful = true;
                        } else if (color.equals("BLACK") && game.blackUsername() != null) {
                            joinSuccessful = true;
                        }
                    } else {
                        // For observers, just verify we can still see the game
                        joinSuccessful = true;
                    }

                    // Debug logging
//                    System.out.println("DEBUG: Game state after join attempt:");
//                    System.out.println("DEBUG: White player: " + game.whiteUsername());
//                    System.out.println("DEBUG: Black player: " + game.blackUsername());
                    break;
                }
            }

            if (!joinSuccessful) {
                return "Failed to join game: Join operation appeared to succeed but game state was not updated";
            }

            String result = "Joined game successfully!";
            System.out.println(result);
            drawChessBoard();
            return result;
        } catch (Exception e) {
            return "Failed to join game: " + e.getMessage();
        }
    }


    private String handleObserveGame() {
        if (state != State.POSTLOGIN) {
            return "Please login first";
        }

        // Auto-list games first
        try {
            String gameList = handleListGames();
            System.out.println(gameList);

            if (gameNumberToId.isEmpty()) {
                return "No games available to observe";
            }
        } catch (Exception e) {
            return "Error listing games: " + e.getMessage();
        }

        System.out.print("Enter game number: ");
        String gameNumberStr = scanner.nextLine().trim();

        try {
            int gameNumber = Integer.parseInt(gameNumberStr);
            if (!gameNumberToId.containsKey(gameNumber)) {
                return "Invalid game number";
            }

            // For Phase 5, just verify game exists and draw the board
            drawChessBoard();
            return "Displaying game " + gameNumber;

        } catch (NumberFormatException e) {
            return "Invalid input. Please enter a valid game number.";
        }
    }

    private void drawChessBoard() {
        // Draw board from black's perspective
        System.out.println("\nBlack's Perspective:");
        drawBoardPerspective(true);

        // Draw board from white's perspective
        System.out.println("\nWhite's Perspective:");
        drawBoardPerspective(false);
    }

    private void drawBoardPerspective(boolean blackPerspective) {
        final String setBgLight = EscapeSequences.SET_BG_COLOR_WHITE;
        final String setBgDark = EscapeSequences.SET_BG_COLOR_BLACK;

        // Print column headers
        System.out.print("    ");
        for (int col = 0; col < 8; col++) {
            int displayCol = blackPerspective ? 7 - col : col;
            char colLabel = (char)('A' + displayCol);
            System.out.print(colLabel + "  ");
        }
        System.out.println();

        for (int row = 0; row < 8; row++) {
            // Convert to displayed row number (1-8)
            int displayRow = blackPerspective ? row + 1 : 8 - row;
            System.out.print(displayRow + "   ");

            for (int col = 0; col < 8; col++) {
                // Calculate the actual board position based on perspective
                int actualRow = blackPerspective ? row : 7 - row;
                int actualCol = blackPerspective ? 7 - col : col;

                boolean isLightSquare = (actualRow + actualCol) % 2 == 0;
                String background = isLightSquare ? setBgLight : setBgDark;
                System.out.print(background);

                // Determine piece based on actual position
                if (actualRow == 1) {
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE + EscapeSequences.BLACK_PAWN);
                } else if (actualRow == 6) {
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_RED + EscapeSequences.WHITE_PAWN);
                } else if (actualRow == 0) {
                    String piece = switch (actualCol) {
                        case 0, 7 -> EscapeSequences.BLACK_ROOK;
                        case 1, 6 -> EscapeSequences.BLACK_KNIGHT;
                        case 2, 5 -> EscapeSequences.BLACK_BISHOP;
                        case 3 -> EscapeSequences.BLACK_QUEEN;
                        case 4 -> EscapeSequences.BLACK_KING;
                        default -> EscapeSequences.EMPTY;
                    };
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE + piece);
                } else if (actualRow == 7) {
                    String piece = switch (actualCol) {
                        case 0, 7 -> EscapeSequences.WHITE_ROOK;
                        case 1, 6 -> EscapeSequences.WHITE_KNIGHT;
                        case 2, 5 -> EscapeSequences.WHITE_BISHOP;
                        case 3 -> EscapeSequences.WHITE_QUEEN;
                        case 4 -> EscapeSequences.WHITE_KING;
                        default -> EscapeSequences.EMPTY;
                    };
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_RED + piece);
                } else {
                    System.out.print(EscapeSequences.EMPTY);
                }
                System.out.print(EscapeSequences.RESET_BG_COLOR);
            }
            System.out.println(EscapeSequences.RESET_ALL);
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