package websocket.commands;

/**
 * Represents a command from a client to leave a game.
 * This is sent when a player or observer wants to stop watching a game.
 */
public class LeaveCommand extends UserGameCommand {
    public LeaveCommand(String authToken, Integer gameID) {
        super(CommandType.LEAVE, authToken, gameID);
    }
}