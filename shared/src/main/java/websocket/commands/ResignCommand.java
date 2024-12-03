package websocket.commands;

/**
 * Represents a command from a client to resign from a game.
 * This can only be sent by players, not observers.
 */
public class ResignCommand extends UserGameCommand {
    public ResignCommand(String authToken, Integer gameID) {
        super(CommandType.RESIGN, authToken, gameID);
    }
}
