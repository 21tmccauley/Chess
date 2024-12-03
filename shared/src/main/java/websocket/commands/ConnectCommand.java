package websocket.commands;

/**
 * Represents a command from a client to connect to a game.
 * This is sent when a player or observer wants to start watching a game.
 */
public class ConnectCommand extends UserGameCommand {
    public ConnectCommand(String authToken, Integer gameID) {
        super(CommandType.CONNECT, authToken, gameID);
    }
}