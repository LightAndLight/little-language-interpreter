package interpreter;

public abstract class Token {
    enum Type {
        ADD,
        MOD,
        MAX,
        OPEN_PAREN,
        CLOSE_PAREN,
        VALUE,
        EOF
    }

    public abstract Type getType();

    public static abstract class Operator extends Token {
        public abstract int eval(int left, int right);

        public static class Add extends Token.Operator {
            public Type getType() {
                return Type.ADD;
            }

            public int eval(int left, int right) {
                return left + right;
            }
        }

        public static class Mod extends Token.Operator {
            public Type getType() {
                return Type.MOD;
            }

            public int eval(int left, int right) {
                return left % right;
            }
        }

        public static class Max extends Token.Operator {
            public Type getType() {
                return Type.MAX;
            }

            public int eval(int left, int right) {
                return Integer.max(left, right);
            }
        }
    }

    public static class Value extends Token {
        private final int value;

        public Value(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public Type getType() {
            return Type.VALUE;
        }
    }

    public static class OpenParen extends Token {
        public Type getType() {
            return Type.OPEN_PAREN;
        }
    }

    public static class CloseParen extends Token {
        public Type getType() {
            return Type.CLOSE_PAREN;
        }
    }

    public static class Eof extends Token {
        public Type getType() {
            return Type.EOF;
        }
    }
}
