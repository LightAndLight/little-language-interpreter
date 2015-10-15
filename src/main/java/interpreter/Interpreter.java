package interpreter;

import java.util.Scanner;

public class Interpreter {
    public static void main(String[] args) {
        Lexer lexer = new Lexer();
        Parser parser = new Parser();

        Scanner scanner = new Scanner(System.in);
        String input = "";
        while (true) {
            System.out.print(">> ");

            input = scanner.nextLine();
            if (input.equals(":q")) {
                break;
            }

            try {
                SyntaxTree tree = parser.parseString(lexer.tokenize(input));
                System.out.println(tree.eval());
            } catch (InterpreterException e) {
                System.out.println("An error occurred");
            }
        }
    }
}
