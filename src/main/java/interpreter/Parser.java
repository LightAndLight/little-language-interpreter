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
        SyntaxTree leftSide = parseLeftSide(input);
        if (!parseToken(input, Token.Type.EOF)) {
            throw new InterpreterException.UnexpectedTokenException(
                    input.getFirst().getType(),
                    Token.Type.EOF);
        }
        return leftSide;
    }

    private boolean parseToken(LinkedList<Token> input, Token.Type type) throws InterpreterException.UnexpectedEndOfInputException {
        if (type != Token.Type.EOF && input.getFirst().getType() == Token.Type.EOF) {
            throw new InterpreterException.UnexpectedEndOfInputException();
        } else if (input.getFirst().getType() != type) {
            return false;
        } else {
            input.removeFirst();
            return true;
        }
    }

    private SyntaxTree parseValue(LinkedList<Token> input) throws InterpreterException.UnexpectedTokenException, InterpreterException.UnexpectedEndOfInputException {
        Token token = input.getFirst();
        if (parseToken(input, Token.Type.VALUE)) {
            return new SyntaxTree.Leaf((Token.Value) token);
        } else {
            throw new InterpreterException.UnexpectedTokenException(
                    token.getType(),
                    Token.Type.VALUE
            );
        }
    }

    private SyntaxTree parseLeftSide(LinkedList<Token> input) throws InterpreterException.UnexpectedTokenException, InterpreterException.UnexpectedEndOfInputException {
        SyntaxTree terminal = parseTerminal(input);
        Optional<SyntaxTree.Branch> maybeRightSide = parseRightSide(input);

        if (maybeRightSide.isPresent()) {
            return maybeRightSide.get().setLeft(terminal);
        }

        return terminal;
    }

    private SyntaxTree parseTerminal(LinkedList<Token> input) throws InterpreterException.UnexpectedTokenException, InterpreterException.UnexpectedEndOfInputException {
        Optional<SyntaxTree> maybeBracketed = parseBracketed(input);
        if (maybeBracketed.isPresent()) {
            return maybeBracketed.get();
        } else {
            return parseValue(input);
        }
    }

    private Optional<SyntaxTree.Branch> parseRightSide(LinkedList<Token> input) throws InterpreterException.UnexpectedTokenException, InterpreterException.UnexpectedEndOfInputException {
        if (input.getFirst().getType() == Token.Type.EOF) {
            return Optional.empty();
        }

        Optional<SyntaxTree.Branch> maybeMax = parseMax(input);
        if (maybeMax.isPresent()) {
            return maybeMax;
        }

        Optional<SyntaxTree.Branch> maybeMod = parseMod(input);
        if (maybeMod.isPresent()) {
            return maybeMod;
        }

        return parseAdd(input);
    }

    private Optional<SyntaxTree> parseBracketed(LinkedList<Token> input) throws InterpreterException.UnexpectedTokenException, InterpreterException.UnexpectedEndOfInputException {
        if (input.size() > 1 && parseToken(input, Token.Type.OPEN_PAREN)) {
            SyntaxTree maybeLeftSide = parseLeftSide(input);
            if (parseToken(input, Token.Type.CLOSE_PAREN)) {
                return Optional.of(maybeLeftSide);
            } else {
                throw new InterpreterException.UnexpectedTokenException(
                        input.getFirst().getType(),
                        Token.Type.CLOSE_PAREN
                );
            }
        }

        return Optional.empty();
    }

    private Optional<SyntaxTree.Branch> parseMax(LinkedList<Token> input) throws InterpreterException.UnexpectedTokenException, InterpreterException.UnexpectedEndOfInputException {
        if (input.size() >= 1 && parseToken(input, Token.Type.MAX)) {
            return Optional.of(
                    new SyntaxTree.Branch.Max(null, parseTerminal(input))
            );
        } else {
            return Optional.empty();
        }
    }

    private Optional<SyntaxTree.Branch> parseMod(LinkedList<Token> input) throws InterpreterException.UnexpectedTokenException, InterpreterException.UnexpectedEndOfInputException {
        if (input.size() >= 1 && parseToken(input, Token.Type.MOD)) {
            return Optional.of(
                    new SyntaxTree.Branch.Mod(null, parseTerminal(input))
            );
        } else {
            return Optional.empty();
        }
    }

    private Optional<SyntaxTree.Branch> parseAdd(LinkedList<Token> input) throws InterpreterException.UnexpectedTokenException, InterpreterException.UnexpectedEndOfInputException {
        if (input.size() >= 1 && parseToken(input, Token.Type.ADD)) {
            return Optional.of(
                    new SyntaxTree.Branch.Add(null, parseTerminal(input))
            );
        } else {
            return Optional.empty();
        }
    }
}
