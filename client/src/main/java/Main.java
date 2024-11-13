import chess.*;
import client.ChessClient;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        var client = new ChessClient(8080);
        var scanner = new Scanner(System.in);

        System.out.println("♕ Welcome to 240 Chess");
        while (true) {
            System.out.print(">>> ");
            String line = scanner.nextLine();
            String result = client.evaluateCommand(line);
            System.out.println(result);
        }
    }
}