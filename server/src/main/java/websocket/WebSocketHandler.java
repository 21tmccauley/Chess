package websocket;

import chess.*;
import com.google.gson.Gson;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.GameService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler {
    // Track active connections and game observers
    private static final Map<Session, ConnectionInfo> connections = new ConcurrentHashMap<>();
    private static final Map<Integer, Set<Session>> gameObservers = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();
    private static GameService gameService;

    public WebSocketHandler(GameService gameService) {
        WebSocketHandler.gameService = gameService;
    }

    // Store connection information
    private static class ConnectionInfo {
        String authToken;
        Integer gameID;
        String username;
        boolean isPlaying;
        ChessGame.TeamColor teamColor;

        ConnectionInfo(String authToken, Integer gameID, String username) {
            this.authToken = authToken;
            this.gameID = gameID;
            this.username = username;
            this.isPlaying = false;
        }
    }

    @WebSocket
    public static class WebSocketEndpoint {
        @OnWebSocketConnect
        public void onConnect(Session session) {
            // Connection established, waiting for CONNECT command
        }

        @OnWebSocketClose
        public void onClose(Session session, int statusCode, String reason) {
            handleDisconnection(session);
        }

        @OnWebSocketMessage
        public void onMessage(Session session, String message) {
            try {
                UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
                handleCommand(session, command);
            } catch (Exception e) {
                sendError(session, "Failed to process command: " + e.getMessage());
            }
        }
    }

    // Handle client disconnection
    private static void handleDisconnection(Session session) {
        ConnectionInfo info = connections.get(session);
        if (info != null) {
            // Remove from game observers
            Set<Session> observers = gameObservers.get(info.gameID);
            if (observers != null) {
                observers.remove(session);
                broadcastNotification(info.gameID, session,
                        String.format("%s has disconnected from the game", info.username));
            }
            connections.remove(session);
        }
    }

    // Determine if user is playing or observing and their team color
    private static void determinePlayerRole(ConnectionInfo info, GameData game) {
        String username = info.username;
        if (username.equals(game.whiteUsername())) {
            info.isPlaying = true;
            info.teamColor = ChessGame.TeamColor.WHITE;
        } else if (username.equals(game.blackUsername())) {
            info.isPlaying = true;
            info.teamColor = ChessGame.TeamColor.BLACK;
        } else {
            info.isPlaying = false;
            info.teamColor = null;
        }
    }

    // Format a chess move into a readable string
    private static String formatMoveDescription(ChessMove move) {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        String promotion = move.getPromotionPiece() != null ?
                " and promoted to " + move.getPromotionPiece() : "";

        return String.format("from %d,%d to %d,%d%s",
                start.getRow(), start.getColumn(),
                end.getRow(), end.getColumn(),
                promotion);
    }

    // Check for game state changes and send notifications
    private static void checkAndNotifyGameState(GameData game) {
        ChessGame chessGame = game.game();

        // Check for white in check/checkmate
        if (chessGame.isInCheck(ChessGame.TeamColor.WHITE)) {
            if (chessGame.isInCheckmate(ChessGame.TeamColor.WHITE)) {
                broadcastNotification(game.gameID(), null, "White is in checkmate! Black wins!");
            } else {
                broadcastNotification(game.gameID(), null, "White is in check!");
            }
        }

        // Check for black in check/checkmate
        if (chessGame.isInCheck(ChessGame.TeamColor.BLACK)) {
            if (chessGame.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                broadcastNotification(game.gameID(), null, "Black is in checkmate! White wins!");
            } else {
                broadcastNotification(game.gameID(), null, "Black is in check!");
            }
        }

        // Check for stalemate
        if (chessGame.isInStalemate(chessGame.getTeamTurn())) {
            broadcastNotification(game.gameID(), null, "Game is in stalemate!");
        }
    }

    private static void handleCommand(Session session, UserGameCommand command) {
        try {
            switch (command.commandType) {
                case CONNECT -> handleConnect(session, command);
                case MAKE_MOVE -> handleMove(session, command);
                case LEAVE -> handleLeave(session, command);
                case RESIGN -> handleResign(session, command);
                case HIGHLIGHT_MOVES -> handleHighlightMoves(session, command);
            }
        } catch (Exception e) {
            sendError(session, e.getMessage());
        }
    }

    private static void handleConnect(Session session, UserGameCommand command) throws Exception {
        // Verify auth token and get game data
        GameData game = gameService.getGame(command.authToken);
        String username = gameService.getUsername(command.authToken);

        // Create and store connection info
        ConnectionInfo info = new ConnectionInfo(command.authToken, command.gameID, username);
        connections.put(session, info);

        // Add to game observers
        gameObservers.computeIfAbsent(command.gameID, k -> ConcurrentHashMap.newKeySet())
                .add(session);

        // Determine if playing or observing
        determinePlayerRole(info, game);

        // Send initial game state
        sendLoadGame(session, game);

        // Notify others of connection
        String roleMessage = info.isPlaying ?
                String.format("joined as %s", info.teamColor) :
                "started observing";
        broadcastNotification(command.gameID, session,
                String.format("%s has %s", username, roleMessage));
    }

    private static void handleMove(Session session, UserGameCommand command) throws Exception {
        ConnectionInfo info = connections.get(session);
        if (!info.isPlaying) {
            throw new Exception("Only players can make moves");
        }

        GameData game = gameService.getGame(command.gameID);
        if (game.game().getTeamTurn() != info.teamColor) {
            throw new Exception("It's not your turn");
        }

        // Validate and make the move
        game.game().makeMove(command.move);
        gameService.updateGame(game);

        // Broadcast updated game state
        broadcastGameUpdate(command.gameID, game);

        // Notify about the move
        String moveDesc = formatMoveDescription(command.move);
        broadcastNotification(command.gameID, null,
                String.format("%s moved %s", info.username, moveDesc));

        // Check for game state changes
        checkAndNotifyGameState(game);
    }

    private static void handleLeave(Session session, UserGameCommand command) {
        handleDisconnection(session);
    }

    private static void handleResign(Session session, UserGameCommand command) throws Exception {
        ConnectionInfo info = connections.get(session);
        if (!info.isPlaying) {
            throw new Exception("Only players can resign");
        }

        GameData game = gameService.getGame(command.gameID);
        // Update game state for resignation
        gameService.resignGame(game.gameID(), info.username);

        // Broadcast final game state
        broadcastGameUpdate(command.gameID, game);

        // Notify of resignation
        broadcastNotification(command.gameID, null,
                String.format("%s has resigned the game", info.username));
    }

    private static void handleHighlightMoves(Session session, UserGameCommand command) throws Exception {
        GameData game = gameService.getGame(command.gameID);
        Collection<ChessMove> validMoves = game.game().validMoves(command.piece);

        // Send moves only to requesting client
        sendHighlightMoves(session, validMoves);
    }

    // Message sending methods
    private static void sendLoadGame(Session session, GameData game) {
        try {
            LoadGameMessage message = new LoadGameMessage(game.game());
            session.getRemote().sendString(gson.toJson(message));
        } catch (IOException e) {
            System.err.println("Failed to send game state: " + e.getMessage());
        }
    }

    private static void sendError(Session session, String errorMessage) {
        try {
            ErrorMessage message = new ErrorMessage(errorMessage);
            session.getRemote().sendString(gson.toJson(message));
        } catch (IOException e) {
            System.err.println("Failed to send error: " + e.getMessage());
        }
    }

    private static void sendHighlightMoves(Session session, Collection<ChessMove> moves) {
        try {
            HighlightMovesMessage message = new HighlightMovesMessage(moves);
            session.getRemote().sendString(gson.toJson(message));
        } catch (IOException e) {
            System.err.println("Failed to send move highlights: " + e.getMessage());
        }
    }

    private static void broadcastGameUpdate(Integer gameID, GameData game) {
        Set<Session> observers = gameObservers.get(gameID);
        if (observers != null) {
            LoadGameMessage message = new LoadGameMessage(game.game());
            String jsonMessage = gson.toJson(message);
            for (Session session : observers) {
                try {
                    session.getRemote().sendString(jsonMessage);
                } catch (IOException e) {
                    System.err.println("Failed to broadcast game update: " + e.getMessage());
                }
            }
        }
    }

    private static void broadcastNotification(Integer gameID, Session except, String message) {
        Set<Session> observers = gameObservers.get(gameID);
        if (observers != null) {
            NotificationMessage notification = new NotificationMessage(message);
            String jsonMessage = gson.toJson(notification);
            for (Session session : observers) {
                if (session != except) {
                    try {
                        session.getRemote().sendString(jsonMessage);
                    } catch (IOException e) {
                        System.err.println("Failed to broadcast notification: " + e.getMessage());
                    }
                }
            }
        }
    }
}