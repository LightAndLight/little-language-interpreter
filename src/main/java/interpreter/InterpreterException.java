package interpreter;

public abstract class InterpreterException extends Exception {
    public static class UnexpectedInputException extends InterpreterException {
        private String input;

        public UnexpectedInputException(String input) {
            this.input = input;
        }

        @Override
        public String toString() {
            return "Unexpected input: " + input;
        }
    }

    public static class UnexpectedEndOfInputException extends InterpreterException {
        @Override
        public String toString() {
            return "Unexpected end of input";
        }
    }

    public static class UnexpectedTokenException extends InterpreterException {
        private Token.Type actual;
        private Token.Type expected;

        public UnexpectedTokenException(Token.Type actual, Token.Type expected) {
            this.actual = actual;
            this.expected = expected;
        }

        @Override
        public String toString() {
            return "Unexpected token: "
                    + actual.toString()
                    + ", expected: "
                    + expected.toString();
        }
    }

    public static class DivideByZeroException extends InterpreterException {
        @Override
        public String toString() {
            return "Division by zero";
        }
    }
}
