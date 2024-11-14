import chess.*;
import client.ChessClient;
import ui.EscapeSequences;
import java.util.Scanner;
import client.ChessClient;
import ui.EscapeSequences;
import java.util.Scanner;

//this is a comment
public class Main {
    private static final String[] CHESS_BANNER = {
            "   ██████╗██╗  ██╗███████╗███████╗███████╗    ██████╗ ██╗  ██╗ ██████╗  ",
            "  ██╔════╝██║  ██║██╔════╝██╔════╝██╔════╝    ██╔══██╗██║  ██║██╔═████╗ ",
            "  ██║     ███████║█████╗  ███████╗███████╗     ╚═██╔╝ ███████║██║██╔██║ ",
            "  ██║     ██╔══██║██╔══╝  ╚════██║╚════██║    ██╔══██╗╚════██║████╔╝██║ ",
            "  ╚██████╗██║  ██║███████╗███████║███████║    ██████╔╝     ██║╚██████╔╝ ",
            "   ╚═════╝╚═╝  ╚═╝╚══════╝╚══════╝╚══════╝    ╚═════╝      ╚═╝ ╚═════╝  "
    };

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

        // Animate the banner
        playStartupAnimation();

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

    private static void playStartupAnimation() {
        try {
            // Number of animation frames
            int frames = 30;

            // Wave parameters
            double amplitude = 3; // How many lines up/down the wave moves
            double frequency = 0.3; // How tight the wave is
            double phaseSpeed = 0.2; // How fast the wave moves

            for (int frame = 0; frame < frames; frame++) {
                System.out.print(EscapeSequences.ERASE_SCREEN);
                System.out.println("\n"); // Add some padding at top

                // For each line in the banner
                for (int lineIndex = 0; lineIndex < CHESS_BANNER.length; lineIndex++) {
                    // Calculate vertical offset for this line based on wave
                    double phase = frame * phaseSpeed;
                    double offset = amplitude * Math.sin(frequency * lineIndex + phase);
                    int spaces = (int) Math.round(offset) + 3;

                    // Add appropriate spacing
                    System.out.print(" ".repeat(Math.max(0, spaces)));

                    // Add bold text for emphasis
                    System.out.print(EscapeSequences.SET_TEXT_BOLD);

                    // Choose color based on frame and line position
                    String color = COLORS[(frame + lineIndex) % COLORS.length];

                    // Print the line with color
                    System.out.println(color + CHESS_BANNER[lineIndex] + EscapeSequences.RESET_ALL);
                }

                Thread.sleep(50); // Control animation speed
            }

            // Final static display
            System.out.print(EscapeSequences.ERASE_SCREEN);
            System.out.println("\n");
            for (int i = 0; i < CHESS_BANNER.length; i++) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE +
                        EscapeSequences.SET_TEXT_BOLD +
                        CHESS_BANNER[i] +
                        EscapeSequences.RESET_ALL);
            }

            // Add decorative elements
            System.out.println("\n" + "═".repeat(75));
            Thread.sleep(300);

            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN +
                    EscapeSequences.SET_TEXT_BOLD +
                    "                         Welcome to the Ultimate Chess Experience" +
                    EscapeSequences.RESET_ALL);

            System.out.println("═".repeat(75) + "\n");
            Thread.sleep(300);

        } catch (InterruptedException e) {
            // Ignore interruptions
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
                        "├─ help        - Show detailed help",
                        "└─ quit        - Exit the program"
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
