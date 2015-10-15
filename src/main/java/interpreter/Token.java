package interpreter;

public abstract class Token {
    enum Type {
        ADD,
        MOD,
        MAX,
        OPEN_PAREN,
        CLOSE_PAREN,
        VALUE
    }

    public abstract Type getType();

    @Override
    public abstract String toString();

    public static abstract class Operator extends Token {
        public abstract int eval(int left, int right);

        public static class Add extends Token.Operator {
            public Type getType() {
                return Type.ADD;
            }

            public String toString() {
                return "ADD";
            }

            public int eval(int left, int right) {
                return left + right;
            }
        }

        public static class Mod extends Token.Operator {
            public Type getType() {
                return Type.MOD;
            }

            public String toString() {
                return "MOD";
            }

            public int eval(int left, int right) {
                return left % right;
            }
        }

        public static class Max extends Token.Operator {
            public Type getType() {
                return Type.MAX;
            }

            public String toString() {
                return "MAX";
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

        public String toString() {
            return "VALUE";
        }
    }

    public static class OpenParen extends Token {
        public Type getType() {
            return Type.OPEN_PAREN;
        }

        public String toString() {
            return "OPEN_PAREN";
        }
    }

    public static class CloseParen extends Token {
        public Type getType() {
            return Type.CLOSE_PAREN;
        }

        public String toString() {
            return "CLOSE_PAREN";
        }
    }
}