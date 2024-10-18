package model;

import chess.ChessGame;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameDataTest {

    @Test
    public void testConstructorAndAccessors(){
        int gameid = 1;
        String wUser = "white";
        String bUser = "black";
        String gameName = "gameName";
        ChessGame game = new ChessGame();

        GameData gameData = new GameData(gameid, wUser,bUser,gameName,game);

        assertEquals(gameid, gameData.gameID());
        assertEquals(wUser, gameData.whiteUsername());
        assertEquals(bUser, gameData.blackUsername());
        assertEquals(gameName, gameData.gameName());
        assertEquals(game, gameData.game());

    }

}