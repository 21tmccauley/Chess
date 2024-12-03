import chess.*;
import client.ChessClient;
import ui.EscapeSequences;
import java.util.Scanner;
import client.ChessClient;
import ui.EscapeSequences;
import java.util.Scanner;

//this is a comment
public class Main {

    private static final String[] COLORS = {
            EscapeSequences.SET_TEXT_COLOR_BLUE,
            EscapeSequences.SET_TEXT_COLOR_MAGENTA,
            EscapeSequences.SET_TEXT_COLOR_GREEN,
            EscapeSequences.SET_TEXT_COLOR_RED,
            EscapeSequences.SET_TEXT_COLOR_YELLOW,
            EscapeSequences.SET_TEXT_COLOR_WHITE
    };

    public static void main(String[] args) {
        var client = new ChessClient(8080);
        var scanner = new Scanner(System.in);

        // Clear screen
        System.out.print(EscapeSequences.ERASE_SCREEN);

        while (true) {
            displayMenu(client.getState() == ChessClient.State.PRELOGIN);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE + ">>> " + EscapeSequences.RESET_TEXT_COLOR);
            String line = scanner.nextLine();

            // Clear screen before showing result
            System.out.print(EscapeSequences.ERASE_SCREEN);

            String result = client.evaluateCommand(line);
            System.out.println(result);

            // Small delay before showing menu again
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }


    private static void displayMenu(boolean isPreLogin) {
        String[] options = isPreLogin ?
                new String[] {
                        "├─ login    - Sign in to your account",
                        "├─ register - Create a new account",
                        "├─ help     - Show detailed help",
                        "└─ quit     - Exit the program"
                } :
                new String[] {
                        "├─ create game - Start a new chess game",
                        "├─ list games  - View available games",
                        "├─ join game   - Play in a game",
                        "├─ observe     - Watch a game",
                        "├─ logout      - Sign out",
                        "└─ help        - Show detailed help"
        };

        // Display current mode
        System.out.println(EscapeSequences.SET_TEXT_COLOR_MAGENTA +
                EscapeSequences.SET_TEXT_BOLD +
                "\n╔══ Current Mode: " + (isPreLogin ? "Guest" : "Logged In") +
                " ══╗" +
                EscapeSequences.RESET_ALL);

        // Display options with tree-like structure
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW +
                EscapeSequences.SET_TEXT_BOLD +
                "Available Commands:" +
                EscapeSequences.RESET_ALL);
        for (String option : options) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN +
                    EscapeSequences.SET_TEXT_BOLD +
                    option +
                    EscapeSequences.RESET_ALL);
        }
        System.out.println();
    }
}
