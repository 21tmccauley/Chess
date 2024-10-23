import chess.*;
import dataaccess.Dataaccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataaccess;
import dataaccess.MySQLDataAccess;
import server.Server;

public class Main {
    public static void main(String[] args) {
        try {
            // Initialize with MySQL instead of Memory implementation
            Dataaccess dataAccess = new MySQLDataAccess();
            Server server = new Server();
            server.initializeDataAccess(dataAccess);
            server.run(8080);

            System.out.println("Server started successfully with MySQL database");
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}