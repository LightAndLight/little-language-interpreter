package interpreter;

public abstract class InterpreterError {
    public static class UnexpectedInputError extends InterpreterError {
        private String input;

        public UnexpectedInputError(String input) {
            this.input = input;
        }

        @Override
        public String toString() {
            return "Unxpected input: " + input;
        }
    }

    public static class UnexpectedTokenError extends InterpreterError {
        private Token actual;
        private Token expected;

        public UnexpectedTokenError(Token actual, Token expected) {
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

    public static class DivideByZeroError extends InterpreterError {
        @Override
        public String toString() {
            return "Division by zero";
        }
    }
}
