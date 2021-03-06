package interpreter;

import java.util.LinkedList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private static abstract class TokenFinder {
        private final Pattern pattern;

        public TokenFinder(String pattern) {
            this.pattern = Pattern.compile("^\\s*(" + pattern + ")\\s*");
        }

        public Matcher getMatcher(String s) {
            return pattern.matcher(s);
        }

        private Optional<Token> returnIfFound(String s, Token token) {
            if (getMatcher(s).find(0)) {
                return Optional.of(token);
            } else {
                return Optional.empty();
            }
        }

        public abstract Optional<Token> getToken(String s);

        public static class OpenParenFinder extends TokenFinder {
            public OpenParenFinder() {
                super("\\(");
            }

            public Optional<Token> getToken(String s) {
                return super.returnIfFound(s, new Token.OpenParen());
            }
        }

        public static class CloseParenFinder extends TokenFinder {
            public CloseParenFinder() {
                super("\\)");
            }

            public Optional<Token> getToken(String s) {
                return super.returnIfFound(s, new Token.CloseParen());
            }
        }

        public static class ValueFinder extends TokenFinder {
            public ValueFinder() {
                super("\\d+");
            }

            public Optional<Token> getToken(String s) {
                Matcher matcher = getMatcher(s);
                if (matcher.find(0)) {
                    return Optional.of(new Token.Value(Integer.parseInt(matcher.group(1))));
                } else {
                    return Optional.empty();
                }
            }
        }

        public static class ModFinder extends TokenFinder {
            public ModFinder() {
                super("%");
            }

            public Optional<Token> getToken(String s) {
                return super.returnIfFound(s, new Token.Operator.Mod());
            }
        }

        public static class MaxFinder extends TokenFinder {
            public MaxFinder() {
                super("\\?");
            }

            public Optional<Token> getToken(String s) {
                return super.returnIfFound(s, new Token.Operator.Max());
            }
        }

        public static class AddFinder extends TokenFinder {
            public AddFinder() {
                super("&");
            }

            public Optional<Token> getToken(String s) {
                return super.returnIfFound(s, new Token.Operator.Add());
            }
        }
    }

    private final TokenFinder[] tokenFinders = {
            new TokenFinder.OpenParenFinder(),
            new TokenFinder.CloseParenFinder(),
            new TokenFinder.ValueFinder(),
            new TokenFinder.MaxFinder(),
            new TokenFinder.ModFinder(),
            new TokenFinder.AddFinder()
    };

    public LinkedList<Token> tokenize(String input) throws InterpreterException.UnexpectedEndOfInputException, InterpreterException.UnexpectedInputException {
        LinkedList<Token> result = new LinkedList<>();
        String remaining = input;
        while (!remaining.isEmpty()) {
            Optional<Token> maybeToken = Optional.empty();
            for (TokenFinder tokenFinder : tokenFinders) {
                maybeToken = tokenFinder.getToken(remaining);
                if (maybeToken.isPresent()) {
                    result.addLast(maybeToken.get());
                    remaining = tokenFinder.getMatcher(remaining).replaceFirst("");
                    break;
                }
            }

            if (!remaining.isEmpty() && !maybeToken.isPresent()) {
                throw new InterpreterException.UnexpectedInputException(remaining);
            }
        }

        result.addLast(new Token.Eof());

        return result;
    }
}
