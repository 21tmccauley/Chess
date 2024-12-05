package client;

import ui.EscapeSequences;

public class MenuUI {
    public void displayMenu(boolean isPreLogin) {
        String[] options = isPreLogin ?
                new String[]{
                        "├─ login    - Sign in to your account",
                        "├─ register - Create a new account",
                        "├─ help     - Show detailed help",
                        "└─ quit     - Exit the program"
                } :
                new String[]{
                        "├─ create game - Start a new chess game",
                        "├─ list games  - View available games",
                        "├─ join game   - Play in a game",
                        "├─ observe     - Watch a game",
                        "├─ logout      - Sign out",
                        "├─ help        - Show detailed help",
                        "└─ quit        - Exit the program"
                };

        System.out.println(EscapeSequences.SET_TEXT_COLOR_MAGENTA +
                EscapeSequences.SET_TEXT_BOLD +
                "\n╔══ Current Mode: " + (isPreLogin ? "Guest" : "Logged In") +
                " ══╗" +
                EscapeSequences.RESET_ALL);

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