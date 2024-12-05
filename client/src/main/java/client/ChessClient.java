package client;

public class ChessClient {
    private final ChessStateManager stateManager;

    public ChessClient(int port) {
        String serverUrl = "http://localhost:" + port;
        this.stateManager = new ChessStateManager(serverUrl);
    }

    public String evaluateCommand(String command) {
        return stateManager.handleCommand(command);
    }

    public ChessStateManager.State getState() {
        return stateManager.getCurrentState();
    }
}