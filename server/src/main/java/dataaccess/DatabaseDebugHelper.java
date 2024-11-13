package dataaccess;

public class DatabaseDebugHelper {
    public static void printDatabaseState() {
        try (var conn = DatabaseManager.getConnection()) {
            System.out.println("\n=== Database State ===");

            // Print users with details
            try (var stmt = conn.prepareStatement("SELECT username, email FROM users")) {
                var rs = stmt.executeQuery();
                System.out.println("\nUsers:");
                while (rs.next()) {
                    System.out.println("  " + rs.getString("username") + " (" + rs.getString("email") + ")");
                }
            }

            // Print games with details
            try (var stmt = conn.prepareStatement("SELECT gameID, gameName, whiteUsername, blackUsername FROM games")) {
                var rs = stmt.executeQuery();
                System.out.println("\nGames:");
                while (rs.next()) {
                    System.out.println("  Game " + rs.getInt("gameID") + ": " + rs.getString("gameName") +
                            " (White: " + rs.getString("whiteUsername") +
                            ", Black: " + rs.getString("blackUsername") + ")");
                }
            }

            // Print auth tokens with details
            try (var stmt = conn.prepareStatement("SELECT authToken, username FROM auth_tokens")) {
                var rs = stmt.executeQuery();
                System.out.println("\nAuth Tokens:");
                while (rs.next()) {
                    System.out.println("  " + rs.getString("username") + ": " + rs.getString("authToken"));
                }
            }

            System.out.println("====================\n");
        } catch (Exception e) {
            System.err.println("Error printing database state: " + e.getMessage());
        }
    }
}