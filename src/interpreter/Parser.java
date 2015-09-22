package interpreter;

import java.util.LinkedList;
import java.util.Optional;

public class Parser {
    // LL(1)
    // 1.	S  -> A
    // 2.	A  -> T A*
    // 3.	A* -> % T | ? T | & T | e
    // 4.	T  -> (A) | number

    public SyntaxTree parseString(LinkedList<Token> input) throws InterpreterException {
        Optional<SyntaxTree> maybeLeftSide = parseLeftSide(input);
        if (maybeLeftSide.isPresent()) {
            return maybeLeftSide.get();
        } else {
            throw new InterpreterException();
        }
    }

    private boolean parseToken(LinkedList<Token> input, Token.Type type) {
        if (input.getFirst().getType() == type) {
            input.removeFirst();
            return true;
        } else {
            return false;
        }
    }

    private Optional<SyntaxTree> parseValue(LinkedList<Token> input) {
        Token token = input.getFirst();
        if (parseToken(input, Token.Type.VALUE)) {
            return Optional.of(new SyntaxTree(token));
        } else {
            return Optional.empty();
        }
    }

    private Optional<SyntaxTree> parseLeftSide(LinkedList<Token> input) {
        return parseTerminal(input).map(
                terminal -> parseRightSide(input).map(
                        tree -> new SyntaxTree(tree.getToken(), terminal, tree.getRight())
                ).orElse(terminal)
        );
    }

    private Optional<SyntaxTree> parseTerminal(LinkedList<Token> input) {
        Optional<SyntaxTree> maybeBracketed = parseBracketed(input);
        if (maybeBracketed.isPresent()) {
            return maybeBracketed;
        } else {
            return parseValue(input);
        }
    }


    private Optional<SyntaxTree> parseRightSide(LinkedList<Token> input) {
        Optional<SyntaxTree> maybeMax = parseMax(input);
        if (maybeMax.isPresent()) {
            return maybeMax;
        }

        Optional<SyntaxTree> maybeMod = parseMod(input);
        if (maybeMod.isPresent()) {
            return maybeMod;
        }

        return parseAdd(input);
    }

    private Optional<SyntaxTree> parseBracketed(LinkedList<Token> input) {
        if (parseToken(input, Token.Type.OPEN_PAREN)) {
            Optional<SyntaxTree> maybeLeftSide = parseLeftSide(input);
            if (parseToken(input, Token.Type.CLOSE_PAREN)) {
                return maybeLeftSide;
            }
        }

        return Optional.empty();
    }

    private Optional<SyntaxTree> parseMax(LinkedList<Token> input) {
        if (input.size() >= 2 && parseToken(input, Token.Type.MAX)) {
            return parseTerminal(input).map(terminal -> new SyntaxTree(new Token.Operator.Max(), null, terminal));
        } else {
            return Optional.empty();
        }
    }

    private Optional<SyntaxTree> parseMod(LinkedList<Token> input) {
        if (input.size() >= 2 && parseToken(input, Token.Type.MOD)) {
            return parseTerminal(input).map(terminal -> new SyntaxTree(new Token.Operator.Mod(), null, terminal));
        } else {
            return Optional.empty();
        }
    }

    private Optional<SyntaxTree> parseAdd(LinkedList<Token> input) {
        if (input.size() >= 2 && parseToken(input, Token.Type.ADD)) {
            return parseTerminal(input).map(terminal -> new SyntaxTree(new Token.Operator.Add(), null, terminal));
        } else {
            return Optional.empty();
        }
    }
}
