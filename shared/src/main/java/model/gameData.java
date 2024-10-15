package model;

import chess.ChessGame;

public record gameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
}
