package server.websocket;

import chess.ChessGame;
import chess.ChessPiece;
import com.google.gson.Gson;
import dataaccess.Dataaccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.*;
import websocket.messages.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandle {
    private static class ConnectionInfo {
        String authToken;
        Integer gameID;
        String username;

        public ConnectionInfo() {}
    }

    private final Map<Integer, Collection<Session>> gameSessions = new ConcurrentHashMap<>();
    private final Map<Session, ConnectionInfo> connectionInfo = new ConcurrentHashMap<>();
    private final Dataaccess dataAccess;
    private final Gson gson;

    public WebSocketHandle(Dataaccess dataAccess) {
        this.dataAccess = dataAccess;
        this.gson = new Gson();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        connectionInfo.put(session, new ConnectionInfo());
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        ConnectionInfo info = connectionInfo.remove(session);
        if (info != null && info.gameID != null) {
            Collection<Session> gameWatchers = gameSessions.get(info.gameID);
            if (gameWatchers != null) {
                gameWatchers.remove(session);
                if (info.username != null) {
                    notifyOthers(info.gameID, session, info.username + " has left the game");
                }
            }
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(session, message);
                case MAKE_MOVE -> handleMove(session, message);
                case LEAVE -> handleLeave(session);
                case RESIGN -> handleResign(session, command);
            }
        } catch (Exception e) {
            sendError(session, "Error processing command: " + e.getMessage());
        }
    }

    private void handleConnect(Session session, String messageJson) throws IOException {
        ConnectCommand command = gson.fromJson(messageJson, ConnectCommand.class);
        try {
            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            GameData game = dataAccess.getGame(command.getGameID());

            ConnectionInfo info = connectionInfo.get(session);
            info.authToken = command.getAuthToken();
            info.gameID = command.getGameID();
            info.username = auth.username();

            gameSessions.computeIfAbsent(command.getGameID(), k -> ConcurrentHashMap.newKeySet())
                    .add(session);

            sendGameState(session, game);

            String roleDescription = determineRole(auth.username(), game);
            notifyOthers(command.getGameID(), session,
                    auth.username() + " has joined the game as " + roleDescription);

        } catch (DataAccessException e) {
            sendError(session, "Error connecting: " + e.getMessage());
        }
    }

    private void handleMove(Session session, String messageJson) throws IOException {
        MakeMoveCommand command = gson.fromJson(messageJson, MakeMoveCommand.class);
        try {
            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            GameData game = dataAccess.getGame(command.getGameID());

            if (game.game().getTeamTurn() == null) {
                throw new DataAccessException("Game is over");
            }

            boolean isWhite = auth.username().equals(game.whiteUsername());
            boolean isBlack = auth.username().equals(game.blackUsername());

            if ((game.game().getTeamTurn() == ChessGame.TeamColor.WHITE && !isWhite) ||
                    (game.game().getTeamTurn() == ChessGame.TeamColor.BLACK && !isBlack)) {
                throw new DataAccessException("Not your turn");
            }

            ChessPiece piece = game.game().getBoard().getPiece(command.getMove().getStartPosition());
            if (piece == null ||
                    (piece.getTeamColor() == ChessGame.TeamColor.WHITE && !isWhite) ||
                    (piece.getTeamColor() == ChessGame.TeamColor.BLACK && !isBlack)) {
                throw new DataAccessException("Can only move your own pieces");
            }

            game.game().makeMove(command.getMove());
            dataAccess.updateGame(game);

            Collection<Session> watchers = gameSessions.get(command.getGameID());
            if (watchers != null) {
                String moveDesc = auth.username() + " moved " + command.getMove().toString();
                for (Session watcher : watchers) {
                    sendGameState(watcher, game);
                }
                notifyOthers(command.getGameID(), session, moveDesc);
            }

        } catch (DataAccessException | chess.InvalidMoveException e) {
            sendError(session, "Error making move: " + e.getMessage());
        }
    }

    private void handleLeave(Session session) throws IOException {
        ConnectionInfo info = connectionInfo.get(session);
        if (info != null && info.gameID != null) {
            Collection<Session> watchers = gameSessions.get(info.gameID);
            if (watchers != null) {
                watchers.remove(session);
                notifyOthers(info.gameID, session, info.username + " has left the game");
            }
        }
    }

    private void handleResign(Session session, UserGameCommand command) throws IOException {
        try {
            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            GameData game = dataAccess.getGame(command.getGameID());

            boolean isPlayer = auth.username().equals(game.whiteUsername()) ||
                    auth.username().equals(game.blackUsername());
            if (!isPlayer) {
                throw new DataAccessException("Only players can resign");
            }

            ChessGame chessGame = game.game();
            chessGame.setTeamTurn(null);
            GameData updatedGame = new GameData(game.gameID(), game.whiteUsername(),
                    game.blackUsername(), game.gameName(),
                    chessGame);
            dataAccess.updateGame(updatedGame);

            String notification = auth.username() + " has resigned from the game";
            NotificationMessage message = new NotificationMessage(notification);
            String messageJson = gson.toJson(message);

            Collection<Session> watchers = gameSessions.get(command.getGameID());
            if (watchers != null) {
                for (Session watcher : watchers) {
                    try {
                        watcher.getRemote().sendString(messageJson);
                    } catch (IOException e) {
                        System.err.println("Failed to notify session: " + e.getMessage());
                    }
                }
            }

        } catch (DataAccessException e) {
            sendError(session, "Error resigning: " + e.getMessage());
        }
    }

    private String determineRole(String username, GameData game) {
        if (username.equals(game.whiteUsername())) {
            return "WHITE player";
        } else if (username.equals(game.blackUsername())) {
            return "BLACK player";
        } else {
            return "an observer";
        }
    }

    private void sendGameState(Session session, GameData gameData) throws IOException {
        LoadGameMessage message = new LoadGameMessage(gameData.game());
        session.getRemote().sendString(gson.toJson(message));
    }

    private void sendError(Session session, String errorMessage) throws IOException {
        ErrorMessage message = new ErrorMessage(errorMessage);
        session.getRemote().sendString(gson.toJson(message));
    }

    private void notifyOthers(Integer gameID, Session exclude, String notification) {
        Collection<Session> watchers = gameSessions.get(gameID);
        if (watchers != null) {
            NotificationMessage message = new NotificationMessage(notification);
            String messageJson = gson.toJson(message);

            for (Session watcher : watchers) {
                if (watcher != exclude) {
                    try {
                        watcher.getRemote().sendString(messageJson);
                    } catch (IOException e) {
                        System.err.println("Failed to notify session: " + e.getMessage());
                    }
                }
            }
        }
    }
}