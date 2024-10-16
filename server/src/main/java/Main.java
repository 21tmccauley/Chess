import chess.*;
import dataAccess.DataAccess;
import dataAccess.MemoryDataAccess;
import server.Server;

public class Main {
    public static void main(String[] args) {
        DataAccess dataAccess = new MemoryDataAccess(); // Or whatever implementation you're using
        Server server = new Server();
        server.run(8080);

        // The following line is kept for demonstration purposes
        // You may remove it if not needed for your specific implementation
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);
    }
}