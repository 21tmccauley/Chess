package client;

public class ChessClient {
    private final ServerFacade server;
    private final State state;

    public ChessClient(int port){
        this.server = new ServerFacade(port);
        this.state = State.PRELOGIN;
    }

    public State getState() {
        return state;
    }
    public String evaluateCommand(String command) {
        if (command.equals("help")) {
            return """
                Available commands:
                - help: Display this help message
                - login: Login with existing account
                - register: Create new account
                - quit: Exit the program
                """;
        }
        return "Invalid command. Type 'help' for a list of commands.";
    }

    public enum State {
        PRELOGIN,
        POSTLOGIN
    }
}
