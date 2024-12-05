import chess.*;
import client.ChessClient;
import client.ChessStateManager;
import client.MenuUI;
import ui.EscapeSequences;
import java.util.Scanner;

public class Main {
    private final ChessClient client;
    private final Scanner scanner;
    private final MenuUI menuUI;

    public Main() {
        this.client = new ChessClient(8080);
        this.scanner = new Scanner(System.in);
        this.menuUI = new MenuUI();
    }

    public void run() {
        System.out.print(EscapeSequences.ERASE_SCREEN);

        while (true) {
            menuUI.displayMenu(client.getState() == ChessStateManager.State.PRELOGIN);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE + ">>> " + EscapeSequences.RESET_TEXT_COLOR);
            String line = scanner.nextLine();

            System.out.print(EscapeSequences.ERASE_SCREEN);

            String result = client.evaluateCommand(line);
            if (result != null) {
                System.out.println(result);
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}